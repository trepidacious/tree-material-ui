package org.rebeam.tree.demo

import cats.syntax.either._
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
      val idString = s"${list.id}"
      val contents = SortableListItem.twoLines(
        s"${list.name}",
        s"${list.items.size} item${if (list.items.size == 1) "" else "s"}"
      )
      SortableListItem(avatarText((idString, list.color)), contents, toList)
    }
  }

  val TodoListsView = ListView[TodoList, TodoPage, TodoPage, FindTodoListById](
    "TodoListView",
    l => FindTodoListById(l.id),
    l => l.id.toString(),
    TodoListSummaryView,
    "Todo lists"
  )

  val TodoItemSummaryView = cursorPView[Todo, Pages[PageWithTodoProjectList, TodoPage]]("TodoItemSummaryView"){
    cp => {
      val item = cp.model
      val toItem = cp.p.set(cp.p.current.toItem(item.id))
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

  val TodoItemsView = ListView[Todo, PageWithTodoProjectList, TodoPage, FindTodoById](
    "TodoItemsView",
    todo => FindTodoById(todo.id),
    todo => todo.id.toString(),
    TodoItemSummaryView,
    "Todo items"
  )

  val TodoProjectEmptyView = PageLayout(MaterialColor.BlueGrey(500), 128, "Loading project...", None, Some(MuiCircularProgress(mode = DeterminateIndeterminate.indeterminate, color = Mui.Styles.colors.white)()), None)

  val TodoProjectView = cursorPView[TodoProject, Pages[TodoPage, TodoPage]]("TodoProjectView") {
    cp => {
      //FIXME use actual creation time
      val fab = PageLayout.addFAB(cp.act(TodoProjectAction.CreateTodoList(Moment(0)): TodoProjectAction))

      val title = Breadcrumbs.container(
        textViewHero(cp.zoomN(TodoProject.name).label("Project name"))
      )

      val contents =
        TodoListsView(
          p => cp.act(TodoProjectAction.ListIndexChange(p.oldIndex, p.newIndex): TodoProjectAction)
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
        TodoItemsView(
          p => cp.act(TodoListAction.TodoIndexChange(p.oldIndex, p.newIndex): TodoListAction)
        )(cp.zoomNP(TodoList.items))

      val buttons = List(
        ToolbarIconButton(
          Mui.SvgIcons.ContentArchive()(),
          cp.act(TodoListAction.Archive: TodoListAction)
        )
      )

      PageLayout(cp.model.color,
        listFAB = Some(fab),
        title = Some(title),
        contents = Some(contents),
        iconButtons = buttons)
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

