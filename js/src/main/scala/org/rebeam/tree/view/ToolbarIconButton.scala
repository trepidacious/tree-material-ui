package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement

import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js

object ToolbarIconButton {

  case class Props(icon: VdomElement, onClick: Callback)

  private val component = ScalaComponent.builder[Props]("ToolbarIcon")
    .render_P(p =>
      MuiIconButton(
        onTouchTap = View.touch(p.onClick),
        touchRippleColor = Mui.Styles.colors.white,
        touchRippleOpacity = 0.5,
        iconStyle = js.Dynamic.literal("color" -> "#FFF")
      )(p.icon)
    ).build


  def apply(icon: VdomElement, onClick: Callback) = component(Props(icon, onClick))
}
