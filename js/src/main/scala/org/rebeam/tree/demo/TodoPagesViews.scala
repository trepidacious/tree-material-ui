package org.rebeam.tree.demo

import cats.data.Xor
import chandu0101.scalajs.react.components.materialui._
import demo.components.BarDemo
import io.circe.Encoder
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.Moment
import org.rebeam.tree.demo.DemoData._
import org.rebeam.tree.demo.DemoRoutes._
import org.rebeam.tree.view.Cursor._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.infinite.Infinite
import org.rebeam.tree.view.measure.{CursorPHeightView, MeasureDemo}
import org.rebeam.tree.view.pages.Pages._
import org.rebeam.tree.view.pages._
import org.rebeam.tree.view.sortable._

import scala.scalajs.js
import scala.scalajs.js.UndefOr

object TodoPagesViews {

  val TodoListSummaryView = cursorPView[TodoList, Pages[TodoPage, TodoPage]]("TodoListSummaryView"){
    cp => {
      val list = cp.model
      val toList = cp.p.set(TodoProjectListPage(list.id))
      val idString = s"${list.id.value}"
      val contents = SortableListItem.twoLines(
        s"${list.name}",
        s"${list.items.size} item${if (list.items.size == 1) "" else "s"}"
      )
      SortableListItem(avatarText((idString, list.color)), contents, toList)
    }
  }
  val SortableTodoListSummaryView = SortableElement.wrap(TodoListSummaryView)

  // Use a height view to get us the height of the rendered element as an extra part of prop.
  // This lets us scale the Infinite list appropriately to fill space.
  val TodoListsView = CursorPHeightView[List[TodoList], Pages[TodoPage, TodoPage]]("TodoListView") {
    (cp, height) =>
      val h: Int = height.map(_.toInt).getOrElse(60)
      // We need to apply a style by class to get the Infinite to be 100% height rather than the
      // "height: containerHeight" inline style it sets on itself. This allows it to resize to fill
      // available space, then be measured by Measure, which adjusts the containerHeight. This is
      // neater than wrapping in a "height: 100%" div, and also works with react-sortable-hoc, which
      // expects the top level component to be the one containing the sortable elements. Using a div
      // breaks this and so breaks the nice feature where dragging to container edge starts scrolling.
      Infinite(elementHeight = 60, containerHeight = h, className = "tree-infinite--height-100-percent")( //useWindowAsScrollContainer = true)(
          MuiSubheader(inset = true, style = js.Dynamic.literal("height" -> "60px"))("Todo lists")
          :: cp.zoomAllMatchesP(l => FindTodoListById(l.id)).zipWithIndex.map {
            case (listCP, index) => SortableTodoListSummaryView(SortableElement.Props(key = listCP.model.id.value, index = index))(listCP)
          }
        )
  }
  val SortableTodoListsView = SortableContainer.wrap(TodoListsView)

  val TodoItemSummaryView = cursorPView[Todo, Pages[PageWithTodoProjectList, TodoPage]]("TodoItemSummaryView"){
    cp => {
      val item = cp.model
      val toItem = cp.p.set(cp.p.current.toItem(item.id))
//      val idString = s"${item.id.value}"
      val contents = SortableListItem.twoLines(
        s"${item.name}",
        s"Priority ${item.priority}"
      )
      val avatar = <.div(
        ^.paddingLeft := "8px",
        booleanViewUnlabelled(cp.zoomN(Todo.completed))
      )

      SortableListItem(avatar, contents, onClickContents = toItem)
    }
  }
  val SortableTodoItemSummaryView = SortableElement.wrap(TodoItemSummaryView)

  val TodoItemsView = CursorPHeightView[List[Todo], Pages[PageWithTodoProjectList, TodoPage]]("TodoItemsView") {
    (cp, height) =>
      val h: Int = height.map(_.toInt).getOrElse(60)
      Infinite(elementHeight = 60, containerHeight = h, className = "tree-infinite--height-100-percent")( //useWindowAsScrollContainer = true)(
        MuiSubheader(inset = true, style = js.Dynamic.literal("height" -> "60px"))("Todo items")
          :: cp.zoomAllMatchesP(t => FindTodoById(t.id)).zipWithIndex.map {
          case (todoCP, index) => SortableTodoItemSummaryView(SortableElement.Props(key = todoCP.model.id.value, index = index))(todoCP)
        }
      )
  }

  val SortableTodoItemsView = SortableContainer.wrap(TodoItemsView)

  val TodoProjectEmptyView = PageLayout(MaterialColor.BlueGrey(500), 128, "Loading project...", None, Some(MuiCircularProgress(mode = DeterminateIndeterminate.indeterminate, color = Mui.Styles.colors.white)()), None)

  val TodoProjectView = cursorPView[TodoProject, Pages[TodoPage, TodoPage]]("TodoProjectView") {
    cp => {
      //FIXME use actual creation time
      val fab = PageLayout.addFAB(cp.act(TodoProjectAction.CreateTodoList(Moment(0)): TodoProjectAction))

      val title = Breadcrumbs.container(
        textViewHero(cp.zoomN(TodoProject.name).label("Project name"))
      )

      val contents =
        SortableTodoListsView(
          SortableContainer.Props(
            onSortEnd = p => cp.act(TodoProjectAction.ListIndexChange(p.oldIndex, p.newIndex): TodoProjectAction),
            useDragHandle = true,
            helperClass = "react-sortable-handler"//,
//            useWindowAsScrollContainer = true
          )
        )(cp.zoomNP(TodoProject.lists))

      PageLayout(MaterialColor.BlueGrey(500), 128, "", Some(fab), Some(title), Some(contents))
    }
  }



  val TodoListView = cursorPView[TodoList, Pages[PageWithTodoProjectList, TodoPage]]("TodoListView") {
    cp => {
      //FIXME use actual creation time
      val fab = PageLayout.addFAB(cp.act(TodoListAction.CreateTodo(Moment(0)): TodoListAction))

      val title =
        Breadcrumbs.container(
//                  Breadcrumbs.element(s"${projectCP.model.name}", projectCP.p.set(TodoProjectPage)),
//                  Breadcrumbs.chevron,
          Breadcrumbs.back(cp.p.set(TodoProjectPage)),
          textViewHero(cp.zoomN(TodoList.name).label("List name"))
        )

      val contents =
        SortableTodoItemsView(
          SortableContainer.Props(
            onSortEnd = p => cp.act(TodoListAction.TodoIndexChange(p.oldIndex, p.newIndex): TodoListAction),
            useDragHandle = true,
            helperClass = "react-sortable-handler"//,
//                      useWindowAsScrollContainer = true
          )
        )(cp.zoomN(TodoList.items).withP(cp.p))

      PageLayout(cp.model.color, 128, "", Some(fab), Some(title), Some(contents), iconButtons = List(
        ToolbarIconButton(Mui.SvgIcons.ContentArchive()(), cp.act(TodoListAction.Archive: TodoListAction))
      ))
    }
  }


  val TodoView = cursorPView[Todo, Pages[TodoProjectListItemPage, TodoPage]]("TodoView") {
    cp => {
      val title =
        Breadcrumbs.container(
          //                  Breadcrumbs.element(s"${projectCP.model.name}", projectCP.p.set(TodoProjectPage)),
          //                  Breadcrumbs.chevron,
          Breadcrumbs.back(cp.p.set(cp.p.current.back)),
          textViewHero(cp.zoomN(Todo.name).label("Todo name"))
        )

      val contents = BarDemo.BarChart(
        BarDemo.Stats(
          values = List(
            List(1, 2.0, 3, 4),
            List(2, 3.0, 1, 4),
            List(2, 2.5, 3, 3)
          ),
          labels = List("2009", "2010", "2011", "2012")
        )
      )
//        <.div(
//          "Contents todo!"
//        )

      PageLayout(MaterialColor.BlueGrey(500), 128, "", None, Some(title), Some(contents), iconButtons = List(
        ToolbarIconButton(Mui.SvgIcons.ActionDelete()(), Callback.empty)
      ))
    }
  }

  val TodoProjectPagesView = PagesView[TodoProject, TodoPage]("TodoProjectPagesView"){
    cp =>
      // Zoom from project to list. If Page is a PageWithTodoProjectList, we
      // will produce a cursor whose model is the specified list, and where
      // Pages have current page type PageWithTodoProjectList
      val list = cp.zoomCT[TodoList, PageWithTodoProjectList](p =>
        cp.zoomN(TodoProject.lists).zoomMatch(FindTodoListById(p.listId))
      )

      val item = cp.zoomCT[Todo, TodoProjectListItemPage](p =>
        cp.zoomN(TodoProject.lists).zoomMatch(FindTodoListById(p.listId)).flatMap(_.zoomN(TodoList.items).zoomMatch(FindTodoById(p.todoId)))
      )

      List[Option[ReactElement]](
        Some(TodoProjectView.withKey(0)(cp)),
        list.map(TodoListView.withKey(1)(_)),
        item.map(TodoView.withKey(2)(_))
      ).flatten
  }

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val todoProjectViewFactory = ServerRootComponent.factory[TodoProject, Pages[TodoPage, TodoPage]](TodoProjectEmptyView, "api/todoproject") {
    TodoProjectPagesView(_)
  }

}

