package org.rebeam.tree.demo

import japgolly.scalajs.react.{ReactComponentB, ReactElement, _}
import japgolly.scalajs.react.extra.router.StaticDsl.{Route, RouteB}
import japgolly.scalajs.react.extra.router._
import org.rebeam.tree.view.Navigation
import org.rebeam.tree.view.pages._
import org.rebeam.tree.demo.DemoData._
import japgolly.scalajs.react.vdom.prefix_<^._

object DemoRoutes {

  sealed trait Page
  case object HomePage          extends Page
  case object AddressPage       extends Page

  sealed trait TodoPage extends Page

  sealed trait PageWithTodoProjectList extends TodoPage {
    def listId: IdOf[TodoList]
    def toItem(todoId: IdOf[Todo]) = TodoProjectListItemPage(listId, todoId)
  }

  sealed trait PageWithTodoProjectListItem extends PageWithTodoProjectList {
    def todoId: IdOf[Todo]
  }

  case object TodoProjectPage extends TodoPage
  case class TodoProjectListPage(listId: IdOf[TodoList]) extends PageWithTodoProjectList
  case class TodoProjectListItemPage(listId: IdOf[TodoList], todoId: IdOf[Todo]) extends PageWithTodoProjectListItem

//    .componentWillReceiveProps(s => Callback{println(s"Current ${s.currentProps}, next ${s.nextProps}")})

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    //Provide a renderer for a view factory using Pages.
    def dynRenderP[P <: Page](g: Pages[P, P] => ReactElement): P => Renderer =
      p => Renderer(r => g(Pages(p, r.narrow[P])))

    def idOf[A] = int.pmap(i => Some(IdOf[A](i)))(_.value)

    def caseObject[A](s: String, a: A) = RouteB.literal(s).xmap(_ => a)(_ => ())

    val todoProjectRoute = caseObject("#todo", TodoProjectPage)
    val todoProjectListRoute = ("#todo/list" / idOf[TodoList]).caseClass[TodoProjectListPage]
    val todoProjectListItemRoute = ("#todo/list" / idOf[TodoList] / "item" / idOf[Todo]).caseClass[TodoProjectListItemPage]

    (trimSlashes
      | staticRoute(root,   HomePage) ~> render(DemoViews.homeView())
      | staticRoute("#address", AddressPage) ~> render(DemoViews.addressView)
//      | staticRoute("#todolist", TodoListPage) ~> render(DemoViews.todoListView)
      | dynamicRouteCT(todoProjectRoute) ~> dynRenderP[TodoPage](DemoViews.todoProjectViewFactory)
      | dynamicRouteCT(todoProjectListRoute) ~> dynRenderP[TodoPage](DemoViews.todoProjectViewFactory)
      | dynamicRouteCT(todoProjectListItemRoute) ~> dynRenderP[TodoPage](DemoViews.todoProjectViewFactory)
      )

      .notFound(redirectToPage(HomePage)(Redirect.Replace))
      .renderWith(layout)
      .verify(HomePage, AddressPage, TodoProjectPage)
  }



  val navs = Map(
    "Home" -> HomePage,
    "Todo List" -> TodoProjectPage,
    "Address" -> AddressPage
  )


  val navigation = Navigation.apply[Page]

  def layout(ctl: RouterCtl[Page], r: Resolution[Page]) = {
    val np = Navigation.Props(ctl, r, r.page, navs)
    navigation(np)
  }

  val baseUrl = BaseUrl.fromWindowOrigin_/

  def router = Router(baseUrl, routerConfig.logToConsole)

}
