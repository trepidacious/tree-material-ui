package org.rebeam.tree.view

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Title {

  private val component = ScalaComponent.builder[String]("Title")
    .render_P(p =>
      <.h1(
        ^.whiteSpace := "nowrap",
        ^.overflow := "hidden",
        ^.textOverflow := "ellipsis",
        ^.margin := "0px",
        ^.paddingTop := "0px",
        ^.letterSpacing := "0px",
        ^.fontSize := "24px",
        ^.fontWeight := "400",
        ^.color := "rgb(255, 255, 255)",
        ^.height := "56px",
        ^.lineHeight := "56px",
        ^.flex := "1 1 0%",
        p
      )
    ).build

  def apply(text: String) = component(text)

}
