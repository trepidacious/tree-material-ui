package org.rebeam.tree.demo

import japgolly.scalajs.react.extra.router._
import org.rebeam.tree.view.Navigation

object DemoRoutes {

  sealed trait Page
  case object Home          extends Page
  case object TodoList      extends Page
  case object Address       extends Page

  val title = "Tree Material UI"

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    (trimSlashes
      | staticRoute(root,   Home) ~> render(DemoViews.homeView())
      | staticRoute("#address", Address) ~> render(DemoViews.addressView)
      | staticRoute("#todolist", TodoList) ~> render(DemoViews.todoListView)
      )

      .notFound(redirectToPage(Home)(Redirect.Replace))
      .renderWith(layout)
      .verify(Home, Address, TodoList)
  }

  val navs = Map(
    "Home" -> Home,
    "Todo List" -> TodoList,
    "Address" -> Address
  )

  val navigation = Navigation.apply[Page]

  def layout(ctl: RouterCtl[Page], r: Resolution[Page]) = {
    val np = Navigation.Props(ctl, r, r.page, navs, title)
    navigation(np)
  }

  val baseUrl = BaseUrl.fromWindowOrigin_/

  def router = Router(baseUrl, routerConfig.logToConsole)

}
