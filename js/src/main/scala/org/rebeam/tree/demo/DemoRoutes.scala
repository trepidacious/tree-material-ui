package org.rebeam.tree.demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.StaticDsl._
import japgolly.scalajs.react.extra.router._
import org.rebeam.tree.view.Navigation
import org.rebeam.tree.view.pages._
import org.rebeam.tree.demo.DemoData._
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.sync._

import japgolly.scalajs.react.vdom.html_<^._

object DemoRoutes {

  sealed trait Page
  case object HomePage          extends Page
  case object AddressPage       extends Page

  case object RefPage           extends Page

//  sealed trait TodoPage extends Page {
//    def back: TodoPage
//  }
//
//  sealed trait PageWithTodoProjectList extends TodoPage {
//    def listId: Id[TodoList]
//    def toItem(todoId: Id[Todo]) = TodoProjectListItemPage(listId, todoId)
//    override def back: TodoPage = TodoProjectPage
//  }
//
//  sealed trait PageWithTodoProjectListItem extends PageWithTodoProjectList {
//    def todoId: Id[Todo]
//    override def back: TodoPage = TodoProjectListPage(listId)
//  }
//
//  case object TodoProjectPage extends TodoPage {
//    override def back: TodoPage = TodoProjectPage
//  }
//
//  case object TodoProjectCachePage extends TodoPage {
//    override def back: TodoPage = TodoProjectCachePage
//  }
//
//  case class TodoProjectListPage(listId: Id[TodoList]) extends PageWithTodoProjectList
//  case class TodoProjectListItemPage(listId: Id[TodoList], todoId: Id[Todo]) extends PageWithTodoProjectListItem
//
//  implicit val transitions = new PagesToTransition[TodoPage] {
//    override def apply(from: TodoPage, to: TodoPage) = {
//      if (from == to.back) PagesTransition.Right else PagesTransition.Left
//    }
//  }

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    //Provide a renderer for a view factory using Pages.
    def dynRenderP[P <: Page](g: Pages[P, P] => VdomElement): P => Renderer =
      p => Renderer(r => g(Pages(p, r.narrow[P])))

//    def id[A] = new RouteB[Id[A]](Id.regex.regex, 1, g => Id.fromString[A](g(0)), Id.toString(_))

    def caseObject[A](s: String, a: A) = RouteB.literal(s).xmap(_ => a)(_ => ())


    val refRoute = caseObject("#ref", RefPage)

//    val todoProjectRoute = caseObject("#todo", TodoProjectPage)
//    val todoProjectListRoute = ("#todo/list" / id[TodoList]).caseClass[TodoProjectListPage]
//    val todoProjectListItemRoute = ("#todo/list" / id[TodoList] / "item" / id[Todo]).caseClass[TodoProjectListItemPage]
//
//    val todoProjectCacheRoute = caseObject("#todocache", TodoProjectCachePage)


    (trimSlashes
      | staticRoute(root,   HomePage) ~> render(DemoViews.homeView())
      | staticRoute("#address", AddressPage) ~> render(DemoViews.addressView)
      | dynamicRouteCT(refRoute) ~> dynRenderP[RefPage.type]((g: Pages[RefPage.type, RefPage.type]) => (RefViews.refViewFactory(g): VdomElement))
//      | dynamicRouteCT(todoProjectRoute) ~> dynRenderP[TodoPage](TodoPagesViews.todoProjectViewFactory)
//      | dynamicRouteCT(todoProjectListRoute) ~> dynRenderP[TodoPage](TodoPagesViews.todoProjectViewFactory)
//      | dynamicRouteCT(todoProjectListItemRoute) ~> dynRenderP[TodoPage](TodoPagesViews.todoProjectViewFactory)
//      | dynamicRouteCT(todoProjectCacheRoute) ~> dynRenderP[TodoPage](TodoPagesViews.todoProjectCacheViewFactory)
      )

      .notFound(redirectToPage(HomePage)(Redirect.Replace))
      .renderWith(layout _ )
      .verify(HomePage, AddressPage)//, TodoProjectPage)
  }



  val navs = List (
    "Home" -> HomePage,
//    "Todo List" -> TodoProjectPage,
    "Address" -> AddressPage
  )

  val navigation = Navigation.apply[Page]

  def layout(ctl: RouterCtl[Page], r: Resolution[Page]) = {
    val np = Navigation.Props(ctl, r, r.page, navs)
    navigation(np)
  }

  val baseUrl = BaseUrl.fromWindowOrigin_/

  def router = Router(baseUrl, routerConfig)

}
