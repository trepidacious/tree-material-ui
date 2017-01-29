package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.View._

import scala.scalajs.js

object PageLayout {

  case class Props(color: Color, height: Int, toolbarText: String, listFAB: Option[ReactElement], title: Option[ReactElement], contents: Option[ReactElement], footer: Option[ReactElement], iconButtons: List[ReactElement])

  private val component = ReactComponentB[Props]("PageLayout")
    .render_P(p => {

      <.div(
        ^.width := "100%",
        ^.height := "100%",
        ^.position := "relative",

        <.div(
          ^.position := "absolute",
          ^.top := "0px",
          ^.left := "0px",
          ^.backgroundColor := p.color,
//        ^.boxSizing := "border-box",
          ^.boxShadow := "rgba(0, 0, 0, 0.117647) 0px 1px 6px, rgba(0, 0, 0, 0.117647) 0px 1px 4px",
          ^.width := "100%",
          ^.zIndex := "1100",
//          ^.display := "flex",
          ^.height := s"${p.height}px",

          // Layout the toolbar with flex
          <.div(
            ^.position := "absolute",
            ^.top := "0px",
            ^.left := "0px",
            ^.width := "100%",
            ^.display := "flex",
            <.div(
              ^.flex := "0 0 72px"
            ),
            Title(p.toolbarText),
            <.div(
              ^.flex := "0 0 auto",
              ^.paddingTop := "4px",
              ^.paddingRight := "4px"
            )(p.iconButtons)
          ),
          <.div(
            ^.position := "absolute",
            ^.top := "56px",
            ^.left := "72px",
            p.title
          )
        ),

        // Optional FAB
        p.listFAB.map(
          fab => <.div(
            ^.position := "absolute",
            ^.zIndex := "1150",
            ^.left := "16px",
            ^.top := s"${p.height - 20}px"
          )(fab)
        ).getOrElse(EmptyTag),

        // Contents
        <.div(
          ^.position := "absolute",
          ^.top := s"${p.height}px",
          ^.bottom := "0px",
          ^.left := "0px",
          ^.width := "100%"
        )(p.contents),

        // Optional footer
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

  def apply(
       color: Color,
       height: Int,
       toolbarText: String = "",
       listFAB: Option[ReactElement] = None,
       title: Option[ReactElement] = None,
       contents: Option[ReactElement] = None,
       footer: Option[ReactElement] = None,
       iconButtons: List[ReactElement] = Nil): RCP[Props] =
    component(Props(color, height, toolbarText, listFAB, title, contents, footer, iconButtons))

  def addFAB(callback: Callback): ReactComponentU_ = {
    MuiFloatingActionButton(
      backgroundColor = MaterialColor.White(),
      mini = true,
      iconStyle = js.Dynamic.literal("fill" -> "rgba(0,0,0, 0.54)"),
      onTouchTap = touch(callback)
    )(Mui.SvgIcons.ContentAdd()())
  }

}
