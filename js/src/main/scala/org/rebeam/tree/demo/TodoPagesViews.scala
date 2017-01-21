package org.rebeam.tree.demo

import cats.data.Xor
import chandu0101.scalajs.react.components.materialui._
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
import org.rebeam.tree.view.pages.Pages._
import org.rebeam.tree.view.pages.{Breadcrumbs, Pages, PagesToTransition, PagesTransition}
import org.rebeam.tree.view.sortable.{SortableContainer, SortableElement, SortableListItem}

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

  val TodoListsView = cursorPView[List[TodoList], Pages[TodoPage, TodoPage]]("TodoListView") {
    cp =>
        // FIXME use zoomAllMatchesP
      Infinite(elementHeight = 60, containerHeight = 400)( //useWindowAsScrollContainer = true)(
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
  val TodoItemsView = cursorPView[List[Todo], Pages[PageWithTodoProjectList, TodoPage]]("TodoItemsView") {
    cp =>
      Infinite(elementHeight = 60, containerHeight = 400)( //useWindowAsScrollContainer = true)(
        MuiSubheader(inset = true, style = js.Dynamic.literal("height" -> "60px"))("Todo items")
          :: cp.zoomAllMatchesP(t => FindTodoById(t.id)).zipWithIndex.map {
          case (todoCP, index) => SortableTodoItemSummaryView(SortableElement.Props(key = todoCP.model.id.value, index = index))(todoCP)
        }
      )
  }
  val SortableTodoItemsView = SortableContainer.wrap(TodoItemsView)



  val TodoProjectEmptyView = TitleBar(MaterialColor.BlueGrey(500), 128, None, Some(MuiCircularProgress(mode = DeterminateIndeterminate.indeterminate, color = Mui.Styles.colors.white)()), None)



  val TodoProjectView = cursorPView[TodoProject, Pages[TodoPage, TodoPage]]("TodoProjectView") {
    cp => {
      //FIXME use actual creation time
      val fab = TitleBar.addFAB(cp.act(TodoProjectAction.CreateTodoList(Moment(0)): TodoProjectAction))

      val title = <.div (
        ^.paddingTop := "64px",
        textViewHero(cp.zoomN(TodoProject.name).label("Project name"))
      )

      val contents = <.div(
        SortableTodoListsView(
          SortableContainer.Props(
            onSortEnd = p => cp.act(TodoProjectAction.ListIndexChange(p.oldIndex, p.newIndex): TodoProjectAction),
            useDragHandle = true,
            helperClass = "react-sortable-handler"//,
//            useWindowAsScrollContainer = true
          )
        )(cp.zoomNP(TodoProject.lists))
      )

      TitleBar(MaterialColor.BlueGrey(500), 128, Some(fab), Some(title), Some(contents))
    }
  }



  val TodoListView = cursorPView[TodoList, Pages[PageWithTodoProjectList, TodoPage]]("TodoListView") {
    cp => {
      //FIXME use actual creation time
      val fab = TitleBar.addFAB(cp.act(TodoListAction.CreateTodo(Moment(0)): TodoListAction))

      val title =
        Breadcrumbs.container(
//                  Breadcrumbs.element(s"${projectCP.model.name}", projectCP.p.set(TodoProjectPage)),
//                  Breadcrumbs.chevron,
          Breadcrumbs.back(cp.p.set(TodoProjectPage)),
          textViewHero(cp.zoomN(TodoList.name).label("List name"))
        )

      val contents =
        <.div(
          SortableTodoItemsView(
            SortableContainer.Props(
              onSortEnd = p => cp.act(TodoListAction.TodoIndexChange(p.oldIndex, p.newIndex): TodoListAction),
              useDragHandle = true,
              helperClass = "react-sortable-handler"//,
//                      useWindowAsScrollContainer = true
            )
          )(cp.zoomN(TodoList.items).withP(cp.p))
        )

      TitleBar(cp.model.color, 128, Some(fab), Some(title), Some(contents))
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

      val contents =
        <.div(
          "Contents todo!"
        )

      TitleBar(MaterialColor.BlueGrey(500), 128, None, Some(title), Some(contents))
    }
  }

  object PagesView {

    case class State(direction: PagesTransition)

    val stateReuse: Reusability[State] = Reusability.byRefOr_==

    class Backend[M, P](scope: BackendScope[CursorP[M, Pages[P, P]], State])(renderToList: CursorP[M, Pages[P, P]] => List[ReactElement])(transitions: PagesToTransition[P]) {

      def render(cp: CursorP[M, Pages[P, P]], state: State): ReactElement = {
        val panes = renderToList(cp)
        // We get an unavoidable extra div from the ReactCssTransitionGroup,
        // so we set a class to allow us to style it with flex etc. using CSS
        <.div(^.className := "tree-pages-view")(
          ReactCssTransitionGroup(
            "tree-pages-view-" + state.direction.className,
            appearTimeout = 550,
            leaveTimeout = 550,
            enterTimeout = 550,
            component = "div")(
            <.div(
              ^.top:="0px",
              ^.width:= "100%",
              ^.height:= "100%",
              ^.position:= "absolute",
              ^.top:= "0",
              ^.left:= "0",
              ^.key:=panes.last.key,
              panes.last
            )
          )
        )
      }
    }

    def apply[M, P](name: String)(renderToList: CursorP[M, Pages[P, P]] => List[ReactElement])(implicit transitions: PagesToTransition[P]) = ReactComponentB[CursorP[M, Pages[P, P]]](name)
      .getInitialState[State](_=> State(PagesTransition.Left))
      .backend(new Backend[M, P](_)(renderToList)(transitions))
      .render(s => s.backend.render(s.props, s.state))
      .componentWillReceiveProps(
        scope => scope.$.setState(State(transitions(scope.currentProps.p.current, scope.nextProps.p.current)))
      )
      .configure(Reusability.shouldComponentUpdate(cursorPReuse, stateReuse))
      .build
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

