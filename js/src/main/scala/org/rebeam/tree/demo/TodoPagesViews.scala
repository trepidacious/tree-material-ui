package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.DeltaIOContextSource
import org.rebeam.tree.demo.DemoData._
import org.rebeam.tree.demo.DemoRoutes._
import org.rebeam.tree.ref.Mirror
import org.rebeam.tree.sync._
import org.rebeam.tree.view.Cursor._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.list.ListItem.EditAndDeleteActions
import org.rebeam.tree.view.pages.Pages._
import org.rebeam.tree.view.pages._
import org.rebeam.tree.view.list._
import Mirror._
import org.rebeam.tree.sync.Sync.{ClientDeltaId, ClientId}

import MuiSvgIcon.SvgIconApply

object TodoPagesViews {

//  val TodoListSummaryView = cursorView[TodoList, Pages[TodoPage, TodoPage]]("TodoListSummaryView"){
//    cp => {
//      val list = cp.model
//      val toList = cp.location.set(TodoProjectListPage(list.id))
//      val idString = s"${list.id}"
//      val contents = SortableListItem.twoLines(
//        s"${list.name}",
//        s"${list.items.size} item${if (list.items.size == 1) "" else "s"}"
//      )
//      SortableListItem(avatarText((idString, list.color)), contents, toList)
//    }
//  }
//
//  val TodoListsView = ListView.legacy[TodoList, TodoPage, TodoPage, FindTodoListById](
//    "TodoListsView",
//    l => FindTodoListById(l.id),
//    l => l.id.toString(),
//    TodoListSummaryView(_),
//    "Todo lists"
//  )
//

  val TodoListSummary = ListItem.listItemWithEditAndDelete[TodoList](
    "TodoListSummary",
    todoList => todoList.name,
    todoList => s"${todoList.items.size} item${if (todoList.items.size == 1) "" else "s"}",
    todoList => avatarArcHashIdentifiedAndColored(todoList)
  )

  val TodoProjectView = ListView.usingId[TodoProject, Pages[TodoPage, TodoPage], TodoList, EditAndDeleteActions](
    "TodoProjectView",
    _.zoom(TodoProject.lists),
    (todoList, todoProjectCursor) => EditAndDeleteActions(
      todoProjectCursor.location.modify(_.toList(todoList.id)),
      todoProjectCursor.act(TodoProjectAction.DeleteListById(todoList.id): TodoProjectAction)
    ),
    TodoListSummary(_),
    "Todo lists",
    mode = ListView.ListMode.Finite
  )


  val TodoSummary = ListItem.listItemWithCompleteEditAndDelete[Todo](
    "TodoSummary",
    todo => todo.name,
    todo => s"Priority ${todo.priority}",
    Todo.completed
  )

  val TodoListView = ListView.usingId[TodoList, Pages[PageWithTodoProjectList, TodoPage], Todo, EditAndDeleteActions](
    "TodoListView",
    _.zoom(TodoList.items),
    (todo, todoListCursor) => EditAndDeleteActions(
      todoListCursor.location.modify(_.toItem(todo.id)),
      todoListCursor.act(TodoListAction.DeleteTodoById(todo.id): TodoListAction)
    ),
    TodoSummary(_),
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

  val TodoProjectPageView = cursorView[TodoProject, Pages[TodoPage, TodoPage]]("TodoProjectView") {
    cp => {
      //FIXME use actual creation time
      val fab = PageLayout.addFAB(cp.act(TodoProjectAction.CreateTodoList(): TodoProjectAction))

      val title = Breadcrumbs.container(
        textViewHero(cp.zoom(TodoProject.name).label("Project name"))
      )

//      val contents =
//        TodoListsView(
//          p => cp.act(TodoProjectAction.ListIndexChange(p.oldIndex, p.newIndex): TodoProjectAction)
//        )(cp.zoom(TodoProject.lists))

      val contents =
        TodoProjectView(
          p => cp.act(TodoProjectAction.ListIndexChange(p.oldIndex, p.newIndex): TodoProjectAction)
        )(cp)

      PageLayout(MaterialColor.BlueGrey(500), 128, "", Some(fab), Some(title), Some(contents))
    }
  }

  val TodoListPageView = cursorView[TodoList, Pages[PageWithTodoProjectList, TodoPage]]("TodoListView") {
    cp => {
      val fab = PageLayout.addFAB(cp.act(TodoListAction.CreateTodo(): TodoListAction))

      val title =
        Breadcrumbs.container(
//                  Breadcrumbs.element(s"${projectCP.model.name}", projectCP.p.set(TodoProjectPage)),
//                  Breadcrumbs.chevron,
          Breadcrumbs.back(cp.location.set(TodoProjectPage)),
          textViewHero(cp.zoom(TodoList.name).label("List name"))
        )

      val contents = TodoListView(
          p => cp.act(TodoListAction.TodoIndexChange(p.oldIndex, p.newIndex): TodoListAction)
      )(cp)

//        TodoItemsView(
//          p => cp.act(TodoListAction.TodoIndexChange(p.oldIndex, p.newIndex): TodoListAction)
//        )(cp.zoomNP(TodoList.items))

      val buttons = List(
        ToolbarIconButton(
          Mui.SvgIcons.ContentArchive()(),
          cp.act(TodoListAction.Archive: TodoListAction)
        ): VdomElement
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


  val TodoView = cursorView[Todo, Pages[TodoProjectListItemPage, TodoPage]]("TodoView") {
    cp => {
      val title =
        Breadcrumbs.container(
          //                  Breadcrumbs.element(s"${projectCP.model.name}", projectCP.p.set(TodoProjectPage)),
          //                  Breadcrumbs.chevron,
          // TODO use a modify method on pages
          Breadcrumbs.back(cp.location.set(cp.location.current.back)),
          textViewHero(cp.zoom(Todo.name).label("Todo name"))
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
      val list = cp.zoomModelAndPageCT[TodoList, PageWithTodoProjectList](p =>
        cp.zoom(TodoProject.lists).zoomMatch(FindTodoListById(p.listId))
      )

      val item = cp.zoomModelAndPageCT[Todo, TodoProjectListItemPage](p =>
        cp.zoom(TodoProject.lists).zoomMatch(FindTodoListById(p.listId)).flatMap(_.zoom(TodoList.items).zoomMatch(FindTodoById(p.todoId)))
      )

      // TODO neater - make list of Option[VdomElement], then zip with index and flatten?
      List[Option[(Key, VdomElement)]](
        Some((0, TodoProjectPageView(cp): VdomElement)),
        list.map(c => (1, TodoListPageView(c): VdomElement)),
        item.map(c => (2, TodoView(c): VdomElement))
      ).flatten
  }

  // TODO why do we need all these acriptions to VdomElement?
  val TodoProjectCachePagesView = cursorView[Mirror, Pages[TodoPage, TodoPage]]("TodoProjectCachePagesView"){
    c => c.followRef(org.rebeam.tree.sync.Ref(Id[TodoProject](Guid(ClientId(0), ClientDeltaId(0), WithinDeltaId(0)))))
          .map(TodoProjectPagesView(_): VdomElement)
          .getOrElse(TodoProjectEmptyView2: VdomElement)
  }

  implicit val contextSource = DeltaIOContextSource.default

  implicit val rootSourceTodoProject = ServerRootComponent.noRootSource[TodoProject]

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val todoProjectViewFactory = ServerRootComponent.factory[TodoProject, Pages[TodoPage, TodoPage]](TodoProjectEmptyView, "api/todoproject") {
    TodoProjectPagesView(_)
  }

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val todoProjectCacheViewFactory = ServerRootComponent.factory[Mirror, Pages[TodoPage, TodoPage]](TodoProjectEmptyView, "api/todoprojectmirror") {
    TodoProjectCachePagesView(_)
  }

}

