package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui._
import demo.components.BarDemo
import io.circe.Encoder
import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree._
import org.rebeam.tree.demo.DemoData._
import org.rebeam.tree.demo.DemoRoutes._
import org.rebeam.tree.ref.Mirror
import org.rebeam.tree.sync._
import org.rebeam.tree.view.Cursor._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.infinite.Infinite
import org.rebeam.tree.view.list.ListItem.EditAndDeleteActions
import org.rebeam.tree.view.measure.{CursorHeightView, MeasureDemo}
import org.rebeam.tree.view.pages.Pages._
import org.rebeam.tree.view.pages._
import org.rebeam.tree.view.list._
import Mirror._
import org.rebeam.tree.ValueDelta.StringValueDelta
import org.rebeam.tree.demo.DemoData.TodoData._
import org.rebeam.tree.sync.Sync
import org.rebeam.tree.sync.Sync.{ClientDeltaId, ClientId, FindById}

import scala.scalajs.js
import scala.scalajs.js.UndefOr

object TodoPagesViews {
  val TodoListSummaryView = cursorView[TodoData, TodoList, TodoListDelta, Pages[TodoPage, TodoPage]]("TodoListSummaryView"){
    cp => {
      val list = cp.model
      val toList = cp.location.set(TodoProjectListPage(list.id))
      val idString = s"${list.id}"
      val contents = SortableListItem.twoLines(
        s"${list.name}",
        s"${list.items.size} item${if (list.items.size == 1) "" else "s"}"
      )
      SortableListItem(avatarText((idString, list.color)), contents, toList)
    }
  }

  val TodoListsView = ListView.legacy[TodoData, TodoData, TodoList, TodoListDelta, TodoPage, TodoPage, FindById[TodoList]](
    "TodoListView",
    l => FindById(l.id),
    l => l.id.toString(),
    TodoListSummaryView,
    "Todo lists"
  )

  val TodoSummary = ListItem.listItemWithCompleteEditAndDelete[TodoData, Todo, TodoDelta](
    "TodoSummary",
    todo => todo.name,
    todo => s"Priority ${todo.priority}",
    TodoDelta.completed
  )

  val TodoListView = ListView.usingId[TodoData, TodoData, TodoList, TodoListDelta, Pages[PageWithTodoProjectList, TodoPage], Todo, TodoDelta, EditAndDeleteActions](
    "TodoListView",
    _.zoom(TodoListDelta.items),
    (todo, todoListCursor) => EditAndDeleteActions(
      todoListCursor.location.modify(_.toItem(todo.id)),
      todoListCursor.act(TodoListDelta.DeleteTodoById(todo.id))
    ),
    TodoSummary,
    "Todo items",
    mode = ListView.ListMode.Finite
  )

  val TodoProjectEmptyView = PageLayout(
    MaterialColor.BlueGrey(500), 128,
    "Loading project...",
    None,
    Some(
      MuiCircularProgress(
        mode = DeterminateIndeterminate.indeterminate,
        color = Mui.Styles.colors.white
      )()
    ),
    None
  )

  val TodoProjectEmptyView2 = PageLayout(
    MaterialColor.BlueGrey(500), 128,
    "Can't find ref...",
    None,
    Some(
      MuiCircularProgress(
        mode = DeterminateIndeterminate.indeterminate,
        color = Mui.Styles.colors.white
      )()
    ),
    None
  )

  val TodoProjectView = cursorView[TodoData, TodoProject, TodoProjectDelta, Pages[TodoPage, TodoPage]]("TodoProjectView") {
    cp => {
      //FIXME use actual creation time
      val fab = PageLayout.addFAB(cp.act(TodoProjectDelta.CreateTodoList()))

      val title = Breadcrumbs.container(
        textViewHero(cp.zoom[Nothing, String, StringValueDelta](TodoProjectDelta.name).label("Project name"))
      )

      val contents =
        TodoListsView(
          p => cp.act(TodoProjectDelta.ListIndexChange(p.oldIndex, p.newIndex))
        )(cp.zoom(TodoProjectDelta.lists))

      PageLayout(MaterialColor.BlueGrey(500), 128, "", Some(fab), Some(title), Some(contents))
    }
  }

  val TodoListPageView = cursorView[TodoData, TodoList, TodoListDelta, Pages[PageWithTodoProjectList, TodoPage]]("TodoListView") {
    cp => {
      val fab = PageLayout.addFAB(cp.act(TodoListDelta.CreateTodo()))

      val title =
        Breadcrumbs.container(
//                  Breadcrumbs.element(s"${projectCP.model.name}", projectCP.p.set(TodoProjectPage)),
//                  Breadcrumbs.chevron,
          Breadcrumbs.back(cp.location.set(TodoProjectPage)),
          textViewHero(cp.zoom[Nothing, String, StringValueDelta](TodoListDelta.name).label("List name"))
        )

      val contents = TodoListView(
          p => cp.act(TodoListDelta.TodoIndexChange(p.oldIndex, p.newIndex))
      )(cp)

//        TodoItemsView(
//          p => cp.act(TodoListAction.TodoIndexChange(p.oldIndex, p.newIndex): TodoListAction)
//        )(cp.zoomNP(TodoList.items))

      val buttons = List(
        ToolbarIconButton(
          Mui.SvgIcons.ContentArchive()(),
          cp.act(TodoListDelta.Archive)
        )
      )

      PageLayout(cp.model.color,
        listFAB = Some(fab),
        title = Some(title),
        contents = Some(contents),
        iconButtons = buttons,
        scrollContents = true
      )
    }
  }


  val TodoView = cursorView[TodoData, Todo, TodoDelta, Pages[TodoProjectListItemPage, TodoPage]]("TodoView") {
    cp => {
      val title =
        Breadcrumbs.container(
          //                  Breadcrumbs.element(s"${projectCP.model.name}", projectCP.p.set(TodoProjectPage)),
          //                  Breadcrumbs.chevron,
          // TODO use a modify method on pages
          Breadcrumbs.back(cp.location.set(cp.location.current.back)),
          textViewHero(cp.zoom[Nothing, String, StringValueDelta](TodoDelta.name).label("Todo name"))
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

  val TodoProjectPagesView = PagesView[TodoData, TodoProject, TodoProjectDelta, TodoPage]("TodoProjectPagesView"){
    cp =>
      // Zoom from project to list. If Page is a PageWithTodoProjectList, we
      // will produce a cursor whose model is the specified list, and where
      // Pages have current page type PageWithTodoProjectList
      val list = cp.zoomModelAndPageCT[TodoList, PageWithTodoProjectList, TodoListDelta](p =>
        cp.zoom(TodoProjectDelta.lists).zoomMatch(FindById[TodoList](p.listId))
      )

      val item = cp.zoomModelAndPageCT[Todo, TodoProjectListItemPage, TodoDelta](p =>
        cp.zoom(TodoProjectDelta.lists).zoomMatch(FindById[TodoList](p.listId)).flatMap(_.zoom(TodoListDelta.items).zoomMatch(FindById[Todo](p.todoId)))
      )

      List[Option[ReactElement]](
        Some(TodoProjectView.withKey(0)(cp)),
        list.map(TodoListPageView.withKey(1)(_)),
        item.map(TodoView.withKey(2)(_))
      ).flatten
  }

//  val TodoProjectCachePagesView = cursorView[Mirror, Pages[TodoPage, TodoPage]]("TodoProjectCachePagesView"){
//    c => c.followRef(org.rebeam.tree.sync.Ref(Id[TodoProject](Guid(ClientId(0), ClientDeltaId(0), WithinDeltaId(0)))))
//          .map(TodoProjectPagesView(_))
//          .getOrElse(TodoProjectEmptyView2)
//  }

  implicit val contextSource = DeltaIOContextSource.default

  implicit val rootSourceTodoProject = ServerRootComponent.noRootSource[TodoData, TodoProject, TodoProjectDelta]

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val todoProjectViewFactory = ServerRootComponent.factory[TodoData, TodoProject, TodoProjectDelta, Pages[TodoPage, TodoPage]](TodoProjectEmptyView, "api/todoproject") {
    TodoProjectPagesView(_)
  }

//  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
//  // changing state when changing pages, so we keep the same websocket etc.
//  val todoProjectCacheViewFactory = ServerRootComponent.factory[Mirror, Pages[TodoPage, TodoPage]](TodoProjectEmptyView, "api/todoprojectmirror") {
//    TodoProjectCachePagesView(_)
//  }
//
}

