package org.rebeam.tree.view

import japgolly.scalajs.react.{ReactComponentB, ReactElement}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.View._

object TitleBar {

  case class Props(color: Color, height: Int, listFAB: Option[ReactElement], title: Option[ReactElement], contents: Option[ReactElement])

  val component = ReactComponentB[Props]("TitleBar")
    .render_P(p => {

      <.div(
        <.div(
          ^.position := "fixed",
          ^.backgroundColor := p.color,
//        ^.boxSizing := "border-box",
          ^.boxShadow := "rgba(0, 0, 0, 0.117647) 0px 1px 6px, rgba(0, 0, 0, 0.117647) 0px 1px 4px",
          ^.width := "100%",
          ^.zIndex := "1100",
          ^.display := "flex",
          ^.height := s"${p.height}px",
          <.div(
            ^.width := "72px",
            ^.height := "16px",
            ^.position := "relative",
            <.div(
              ^.position := "absolute",
              ^.left := "16px",
              ^.top := "108px"
            )(p.listFAB)
          ),
          <.div(
            p.title
          )
        ),

        // Contents
        <.div(
          ^.paddingTop := s"${p.height}px"
        )(p.contents)
      )

    })
    .build

  def apply(color: Color, height: Int, listFAB: Option[ReactElement], title: Option[ReactElement], contents: Option[ReactElement]) =
    component(Props(color, height, listFAB, title, contents))

}
