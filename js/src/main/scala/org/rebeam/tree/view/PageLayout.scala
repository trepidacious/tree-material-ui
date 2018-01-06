package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import MuiSvgIcon.SvgIconApply
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.view.View._

import scala.scalajs.js

object PageLayout {

  case class Props(
    color: Color,
    height: Int,
    toolbarText: String,
    listFAB: Option[VdomElement],
    title: Option[VdomElement],
    contents: Option[VdomElement],
    footer: Option[VdomElement],
    iconButtons: List[VdomElement],
    backgroundColor: Color,
    scrollContents: Boolean
  )

  private val component = ScalaComponent.builder[Props]("PageLayout")
    .render_P(p => {

      //Full size container
      <.div(
        ^.width := "100%",
        ^.height := "100%",

        // Optional FAB - this animates by scaling
        p.listFAB.map(
          fab => <.div(
            ^.className := "tree-pages-view__content--scale",
            ^.position := "absolute",
            ^.left := "16px",
            ^.top := s"${p.height - 20}px",
            ^.zIndex := "1150"
          )(fab)
        ).getOrElse(EmptyVdom),

        //Everything else animates by sliding
        <.div(
          ^.className := "tree-pages-view__content--slide",
          ^.width := "100%",
          ^.height := "100%",
          ^.position := "relative",

          <.div(
            ^.position := "absolute",
            ^.top := "0px",
            ^.left := "0px",
            ^.backgroundColor := p.color.toString,
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
                ^.paddingRight := "4px",
                p.iconButtons.toTagMod
              )
            ),
            <.div(
              ^.position := "absolute",
              ^.top := "56px",
              ^.left := "72px",
              p.title.whenDefined
            )
          ),

          // Contents
          if (p.scrollContents) {
            <.div(
              ^.position := "absolute",
              ^.top := s"${p.height}px",
              ^.bottom := "0px",
              ^.left := "0px",
              ^.width := "100%",
              ^.backgroundColor := p.backgroundColor.toString,
              ^.overflowX := "hidden",
              ^.overflowY := "scroll",
              ^.style := js.Dictionary(
                "WebkitOverflowScrolling" -> "touch"
              ),
              p.contents.whenDefined
            )
          } else {
            <.div(
              ^.position := "absolute",
              ^.top := s"${p.height}px",
              ^.bottom := "0px",
              ^.left := "0px",
              ^.width := "100%",
              ^.backgroundColor := p.backgroundColor.toString,
              p.contents.whenDefined
            )
          },

          // Optional footer
          p.footer.map(
            tb => <.div(
              ^.position := "fixed",
              ^.width := "100%",
              ^.bottom := "0px",
              ^.left := "0px",
              tb
            )
          ).getOrElse(EmptyVdom)

        )
      )
    })
    .build

  def apply(
       color: Color,
       height: Int = 128,
       toolbarText: String = "",
       listFAB: Option[VdomElement] = None,
       title: Option[VdomElement] = None,
       contents: Option[VdomElement] = None,
       footer: Option[VdomElement] = None,
       iconButtons: List[VdomElement] = Nil,
       backgroundColor: Color = MaterialColor.White(),
       scrollContents: Boolean = false) =
    component(Props(color, height, toolbarText, listFAB, title, contents, footer, iconButtons, backgroundColor, scrollContents))

  def addFAB(callback: Callback) = {
    MuiFloatingActionButton(
      backgroundColor = MaterialColor.White(),
      mini = true,
      iconStyle = js.Dynamic.literal("fill" -> "rgba(0,0,0, 0.54)"),
      onTouchTap = touch(callback)
    )(Mui.SvgIcons.ContentAdd()())
  }

}
