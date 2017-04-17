package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui.{Mui, MuiIconButton}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, ReactElement}
import org.rebeam.tree.view.{RCP, View}

import scala.scalajs.js

object ListItemIconButton {

  case class Props(icon: ReactElement, onClick: Callback)

  private val component = ReactComponentB[Props]("ListItemIconButton")
    .render_P(p =>
      <.div(
        ^.margin := "4px 4px",
        MuiIconButton(
          onTouchTap = View.touch(p.onClick),
          touchRippleColor = Mui.Styles.colors.white,
          touchRippleOpacity = 0.5,
          iconStyle = js.Dynamic.literal("color" -> "#FFF")
        )(p.icon)
      )
    ).build

  def apply(icon: ReactElement, onClick: Callback): RCP[Props] = component(Props(icon, onClick))
}
