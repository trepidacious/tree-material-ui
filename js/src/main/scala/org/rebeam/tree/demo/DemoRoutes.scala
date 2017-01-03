package org.rebeam.tree.demo

import japgolly.scalajs.react.{ReactComponentB, ReactElement, _}
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import japgolly.scalajs.react.extra.router._
import org.rebeam.tree.view.Navigation
import org.rebeam.tree.view.pages._
import org.rebeam.tree.demo.DemoData._
import japgolly.scalajs.react.vdom.prefix_<^._

object DemoRoutes {

  sealed trait Page
  case object HomePage          extends Page
  case object TodoListPage      extends Page
  case object AddressPage       extends Page
  case class ItemPage(id: Int) extends Page

  sealed trait TodoPage extends Page

  case object TodoProjectPage extends TodoPage
  case class TodoProjectListPage(listId: TodoListId) extends TodoPage
  case class TodoProjectListItemPage(listId: TodoListId, todoId: TodoId) extends TodoPage

  val title = "Tree Material UI"

  val itemPage = ReactComponentB[ItemPage]("Item page")
    .render(p => <.div(s"Info for item #${p.props.id}"))
    .build

  val todoPage = ReactComponentB[TodoPage]("Todo page")
    .render(p => p.props match {
      case TodoProjectPage => <.div("Todo project")
      case TodoProjectListPage(listId) => <.div(s"Todo list #$listId")
      case TodoProjectListItemPage(listId, todoId) => <.div(s"Todo list #$listId, item #$todoId")
    })
    .componentWillReceiveProps(s => Callback{println(s"Current ${s.currentProps}, next ${s.nextProps}")})
    .build



  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

//    def renderP[A](g: RouterCtl[Page] => A)(implicit toRE: A => ReactElement): Renderer =
//      Renderer(r => toRE(g(r)))

    //Provide a renderer for a view factory using Pages.
    def dynRenderP[P <: Page](g: Pages[P] => ReactElement): P => Renderer =
      p => Renderer(r => g(Pages(p, r.narrow[P])))

    val todoListId = int.pmap(i => Some(TodoListId(i)))(_.value)
    val todoId = int.pmap(i => Some(TodoId(i)))(_.value)

    val todoProjectListItemRoute = ("#todo/list" / todoListId / "item" / todoId).caseClass[TodoProjectListItemPage]
    val todoProjectListRoute = ("#todo/list" / todoListId).caseClass[TodoProjectListPage]

    (trimSlashes
      | staticRoute(root,   HomePage) ~> render(DemoViews.homeView())
      | staticRoute("#address", AddressPage) ~> render(DemoViews.addressView)
      | staticRoute("#todolist", TodoListPage) ~> render(DemoViews.todoListView)
      | dynamicRouteCT("#item" / int.caseClass[ItemPage]) ~> dynRender(itemPage(_))
      // We use the "R" prefix to get access to the RouterCtl in the render, and pass it through in the PageCursor
      | staticRoute("#todo", TodoProjectPage) ~> renderR(routerCtl => DemoViews.todoProjectViewFactory(Pages(TodoProjectPage, routerCtl.narrow[TodoPage])))
      | dynamicRouteCT(todoProjectListRoute) ~> dynRenderP[TodoPage](DemoViews.todoProjectViewFactory)
      | dynamicRouteCT(todoProjectListItemRoute) ~> dynRenderP[TodoPage](DemoViews.todoProjectViewFactory)
      )

      .notFound(redirectToPage(HomePage)(Redirect.Replace))
      .renderWith(layout)
      .verify(HomePage, AddressPage, TodoListPage)
  }

  val navs = Map(
    "Home" -> HomePage,
    "Todo List" -> TodoListPage,
    "Address" -> AddressPage
  )


  val navigation = Navigation.apply[Page]

  def layout(ctl: RouterCtl[Page], r: Resolution[Page]) = {
    val np = Navigation.Props(ctl, r, r.page, navs, title)
    navigation(np)
  }

  val baseUrl = BaseUrl.fromWindowOrigin_/

  def router = Router(baseUrl, routerConfig.logToConsole)

}
