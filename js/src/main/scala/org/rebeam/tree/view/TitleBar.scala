package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.View._

import scala.scalajs.js

object TitleBar {

  case class Props(color: Color, height: Int, listFAB: Option[ReactElement], title: Option[ReactElement], contents: Option[ReactElement], footer: Option[ReactElement])

  val component = ReactComponentB[Props]("TitleBar")
    .render_P(p => {

      <.div(
        ^.width := "100%",

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
          ^.paddingTop := s"${p.height}px",
          ^.width := "100%"
        )(p.contents),

        //Optional footer
        p.footer.map(
          tb => <.div(
            ^.position := "fixed",
            ^.width := "100%",
            ^.bottom := "0px",
            ^.left := "0px",
            tb
          )
        ).getOrElse(EmptyTag)

      )

    })
    .build

  def apply(color: Color, height: Int, listFAB: Option[ReactElement], title: Option[ReactElement], contents: Option[ReactElement], footer: Option[ReactElement] = None) =
    component(Props(color, height, listFAB, title, contents, footer))

  def addFAB(callback: Callback) = {
    MuiFloatingActionButton(
      backgroundColor = MaterialColor.White(),
      mini = true,
      iconStyle = js.Dynamic.literal("fill" -> "rgba(0,0,0, 0.54)"),
      onTouchTap = touch(callback)
    )(Mui.SvgIcons.ContentAdd()())
  }

}
