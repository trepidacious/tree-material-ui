package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js

object Navigation {

  case class Props[P](routerCtl: RouterCtl[P], resolution: Resolution[P], page: P, navs: Map[String, P], title: String)

  case class State(drawerOpen: Boolean)

  class Backend[P](scope: BackendScope[Props[P], State]) {

    def render(p: Props[P], s: State) = {
      <.div(
        MuiAppBar(
          title = p.title: ReactNode,
//          onLeftIconButtonTouchTap  = CallbackDebug.f1("onLeftIconButtonTouchTap"),
//          onRightIconButtonTouchTap = CallbackDebug.f1("onRightIconButtonTouchTap"),
//          onTitleTouchTap           = CallbackDebug.f1("onTitleTouchTap"),
          showMenuIconButton = true,
          style = js.Dynamic.literal(
            "position" -> "fixed",
            "top" -> "0px"
          )
        )(),
        <.div(
          ^.paddingTop := "64px",
          <.ul(
            p.navs.map {
              case (name, page) => <.li(
                (if (p.page == page) ">" else " ") + name,
                p.routerCtl setOnClick page
              )
            }
          ),
          p.resolution.render()
        )
      )
    }
  }

  //TODO make only the contents care about resolution
  //Reusable if all fields are equal except routerCtl, where we use its own reusability
  implicit def navPropsReuse[P]: Reusability[Props[P]] = Reusability.fn{
    case (a, b) if a eq b => true // First because most common case and fastest
    case (a, b) if a.page == b.page && a.navs == b.navs && a.title == b.title && a.resolution == b.resolution => RouterCtl.reusability[P].test(a.routerCtl, b.routerCtl)
    case _ => false
  }

  //Just make the component constructor - props to be supplied later to make a component
  def apply[P] = ReactComponentB[Props[P]]("Nav")
    .initialState(State(false))
    .backend(new Backend[P](_))
    .render(s => s.backend.render(s.props, s.state))
    .build




//  def navMenu[P] = ReactComponentB[NavProps[P]]("Menu")
//    .render_P { p =>
//
//      def nav(name: String, target: P) = {
//        <.nav(
//          ^.cls := "mdl-navigation mdl-layout--large-screen-only",
//          ^.key := name,
//          <.span(
//            ^.classSet1(
//              "mdl-navigation__link",
//              "mdl-navigation__link--active" -> (p.page == target)
//            ),
//            name,
//            p.routerCtl setOnClick target
//          )
//        )
//      }
//
//      <.header(
//        ^.cls := "mdl-layout__header",
//        <.div(
//          ^.cls := "mdl-layout__header-row",
//
//          // Title
//          <.span(^.cls := "mdl-layout-title", p.title),
//
//          // Add spacer, to align navigation to the right
//          <.div(^.cls := "mdl-layout-spacer")
//        )(
//          // Navigation. We hide it in small screens.
//          p.navs.map(n => nav(n._1, n._2))
//        )
//      )
//    }
//    .configure(Reusability.shouldComponentUpdate)
//    .build
//
//  def navDrawer[P] = ReactComponentB[NavProps[P]]("Drawer")
//    .render_P { p =>
//
//      def nav(name: String, target: P) = {
//        <.span(
//          ^.classSet1(
//            "mdl-navigation__link",
//            "mdl-navigation__link--active" -> (p.page == target)
//          ),
//          ^.key := name,
//          name,
//          p.routerCtl setOnClick target
//        )
//      }
//
//      <.div(
//        ^.cls := "mdl-layout__drawer",
//        <.span(
//          ^.cls := "mdl-layout-title",
//          p.title
//        ),
//        <.nav(^.cls := "mdl-navigation")(
//          p.navs.map(n => nav(n._1, n._2))
//        )
//      )
//    }
//    .configure(Reusability.shouldComponentUpdate)
//    .build
//
//  val navLayout = <.div(^.cls := "mdl-layout mdl-js-layout mdl-layout--fixed-header")
//
//  def navContents(r: ReactElement) =
//    <.main(
//      ^.cls := "mdl-layout__content mdl-color-text--grey-600",
//      <.div(
//        ^.cls := "page-content",
//        r
//      )
//    )

}
