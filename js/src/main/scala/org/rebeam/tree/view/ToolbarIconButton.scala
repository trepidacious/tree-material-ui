package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._

import scala.scalajs.js

object ToolbarIconButton {

  case class Props(icon: ReactElement, onClick: Callback)

  private val component = ReactComponentB[Props]("ToolbarIcon")
    .render_P(p =>
      MuiIconButton(
        onTouchTap = View.touch(p.onClick),
        touchRippleColor = Mui.Styles.colors.white,
        touchRippleOpacity = 0.5,
        iconStyle = js.Dynamic.literal("color" -> "#FFF")
      )(p.icon)
    ).build

  def apply(icon: ReactElement, onClick: Callback): RCP[Props] = component(Props(icon, onClick))
}
