package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import MuiSvgIcon.SvgIconApply

import scala.scalajs.js

object Navigation {

  case class Props[P](routerCtl: RouterCtl[P], resolution: Resolution[P], page: P, navs: List[(String, P)])

  case class State(drawerOpen: Boolean)

  class Backend[P](scope: BackendScope[Props[P], State]) {

    val toggleDrawerOpen = scope.modState(s => s.copy(drawerOpen = !s.drawerOpen))

    val onRequestChange: (Boolean, String) => Callback =
      (open, reason) =>
        Callback.info(s"onRequestChange: open: $open, reason: $reason") >>
          scope.modState(s => s.copy(drawerOpen = !s.drawerOpen))


    def render(p: Props[P], s: State) = {
      <.div(
        <.div(
          ^.position := "fixed",
          ^.top := "4px",
          ^.left := "4px",
          ^.zIndex := "1200",
          MuiIconButton(onTouchTap = View.touch(toggleDrawerOpen), iconStyle = js.Dynamic.literal("color" -> "#FFF"))(Mui.SvgIcons.NavigationMenu()())
        ),
//        MuiAppBar(
////          title = p.title: VdomNode,
//          onLeftIconButtonTouchTap  = View.touch(toggleDrawerOpen),
////          onRightIconButtonTouchTap = CallbackDebug.f1("onRightIconButtonTouchTap"),
////          onTitleTouchTap           = CallbackDebug.f1("onTitleTouchTap"),
//          showMenuIconButton = true,
//          style = js.Dynamic.literal(
//            "position" -> "fixed",
//            "top" -> "0px",
//            "box-shadow" -> "none",
//            "background-color" -> "none"
//          )
//        )(),
        MuiDrawer(
          onRequestChange = onRequestChange,  //Toggle open state
          docked          = false,
          open            = s.drawerOpen
        )(
          //TODO get the color from Material-UI theme, or is there a component that does this?
//          <.div(
//            ^.backgroundColor := "#757575",
//            ^.color           := "rgb(255, 255, 255)",
//            ^.height          := "64px"
//          ),
          MuiMenu()(
            p.navs.map {
              case (name, page) =>
                MuiMenuItem(
                  key         = name,
                  primaryText = name: VdomNode,
                  checked     = p.page == page,
                  insetChildren = p.page != page,   //Allow space for icon/checkmark when it's not displayed
                  onTouchTap  = View.touch(p.routerCtl.set(page) >> toggleDrawerOpen),
                  style       = js.Dynamic.literal(
                    "cursor" -> "pointer",
                    "user-select" -> "none"
                  )
                )(): VdomNode
            } : _*
          )
        ),
//        <.div(
//            ^.paddingTop    := "64px",
            p.resolution.render()
//        )
      )
    }
  }

  //TODO make only the contents care about resolution
  //Reusable if all fields are equal except routerCtl, where we use its own reusability
  implicit def navPropsReuse[P]: Reusability[Props[P]] = Reusability {
    case (a, b) if a eq b => true // First because most common case and fastest
    case (a, b) if a.page == b.page && a.navs == b.navs && a.resolution == b.resolution => RouterCtl.reusability[P].test(a.routerCtl, b.routerCtl)
    case _ => false
  }

  //Just make the component constructor - props to be supplied later to make a component
  def apply[P] = ScalaComponent.builder[Props[P]]("Nav")
    .initialState(State(false))
    .backend(new Backend[P](_))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
