package org.rebeam.tree.demo

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import japgolly.scalajs.react.extra.router._
import org.rebeam.tree.view.Navigation
import org.rebeam.tree.view.pages._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react._

object DemoRoutes {

  sealed trait Page
  case object HomePage          extends Page
  case object TodoListPage      extends Page
  case object AddressPage       extends Page
  case class ItemPage(id: Int) extends Page

  sealed trait TodoPage extends Page

  case object TodoProjectPage extends TodoPage
  case class TodoProjectListPage(listId: Int) extends TodoPage
  case class TodoProjectListItemPage(listId: Int, todoId: Int) extends TodoPage

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

    //Dynamic route can also be:
    //      | dynamicRoute("#item" / int.caseClass[ItemPage]){case p: ItemPage => p} ~> dynRender(itemPage(_))

    val todoProjectListItemRoute: Route[TodoProjectListItemPage] = ("#todo/list" / int / "item" / int).caseClass[TodoProjectListItemPage]
    val todoProjectListRoute: Route[TodoProjectListPage] = ("#todo/list" / int).caseClass[TodoProjectListPage]


    (trimSlashes
      | staticRoute(root,   HomePage) ~> render(DemoViews.homeView())
      | staticRoute("#address", AddressPage) ~> render(DemoViews.addressView)
      | staticRoute("#todolist", TodoListPage) ~> render(DemoViews.todoListView)
      | dynamicRouteCT("#item" / int.caseClass[ItemPage]) ~> dynRender(itemPage(_))
      // We use the "R" prefix to get access to the RouterCtl in the render, and pass it through in the PageCursor
      | staticRoute("#todo", TodoProjectPage) ~> renderR(routerCtl => DemoViews.todoProjectViewFactory(Pages(TodoProjectPage, p => routerCtl.set(p))))
      | dynamicRouteCT(todoProjectListRoute) ~> dynRenderR((p, routerCtl) => DemoViews.todoProjectViewFactory(Pages(p, p => routerCtl.set(p)  )))
      | dynamicRouteCT(todoProjectListItemRoute) ~> dynRenderR((p, routerCtl) => DemoViews.todoProjectViewFactory(Pages(p, p => routerCtl.set(p)  )))
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
