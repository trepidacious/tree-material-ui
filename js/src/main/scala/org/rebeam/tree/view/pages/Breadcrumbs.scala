package org.rebeam.tree.view.pages

import chandu0101.scalajs.react.components.materialui.{Mui, MuiIconButton}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react._
import org.rebeam.tree.view.Color
import org.rebeam.tree.view.View._

import scala.scalajs.js

object Breadcrumbs {

  val container = ReactComponentB[Unit]("PathBreadcrumbs")
    .render_C(children => {
      <.div(
        ^.paddingTop := "8px",
        ^.display := "flex",
        ^.alignItems := "baseline"
      )(children)
    })
    .build

  val chevron =
    <.div(
      ^.width := "40px",
      ^.height := "20px",
      ^.position := "relative",
      Mui.SvgIcons.NavigationChevronRight(
        color = Color(255,255,255, 137),
        style = js.Dynamic.literal(
          "position" -> "absolute",
          "top" -> "0px",
          "left" -> "8px"
        )
      )()
    )

    case class PathElementProps(content: String, callback: Callback)

    private val pathElementComponent = ReactComponentB[PathElementProps]("PathElement")
      .render_P(p => {
        <.span(
          ^.color := "rgba(255, 255, 255, 0.54)",
          ^.fontSize := "24px",
          ^.cursor := "pointer",
          ^.onClick --> p.callback,
          p.content
        )
      })
      .build

    def element(content: String, callback: Callback) =
      pathElementComponent(PathElementProps(content, callback))


    case class PathBackProps(callback: Callback)

    private val pathBackComponent = ReactComponentB[PathBackProps]("PathBack")
      .render_P(p =>

        <.div(
          ^.width := "48px",
          ^.height := "20px",
          ^.position := "relative",
          MuiIconButton(
            style = js.Dynamic.literal(
              "position" -> "absolute",
              "top" -> "-12px",
              "left" -> "0px"
            ),
            onTouchTap = touch(p.callback)
          )(
            Mui.SvgIcons.NavigationChevronLeft(
              color = Color(255,255,255)
            )()
          )
        )
      ).build

    def back(callback: Callback) =
      pathBackComponent(PathBackProps(callback))

}
