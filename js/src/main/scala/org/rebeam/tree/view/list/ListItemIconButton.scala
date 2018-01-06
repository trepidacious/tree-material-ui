package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui.{Mui, MuiIconButton}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import org.rebeam.tree.view.View

import scala.scalajs.js

object ListItemIconButton {

  case class Props(icon: VdomElement, onClick: Callback)

  private val component = ScalaComponent.builder[Props]("ListItemIconButton")
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

  def apply(icon: VdomElement, onClick: Callback) = component(Props(icon, onClick))
}
