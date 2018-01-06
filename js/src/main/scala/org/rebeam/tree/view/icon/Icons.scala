package org.rebeam.tree.view.icon

import japgolly.scalajs.react.vdom.TagOf
import org.scalajs.dom.html.Div
import org.scalajs.dom.svg.SVG

object Icons {

  def makeWhiteIcon(transform: String, paths: String*): TagOf[SVG] = {
    import japgolly.scalajs.react.vdom.svg_<^._
    <.svg(
      ^.width := "24px",
      ^.height := "24px",
      ^.color := "#FFF",
      ^.fill := "#FFF",
      ^.viewBox := "0 0 24 24",
      paths.toTagMod(path => <.path(^.transform := transform, ^.d := path))
    )
  }

  def makeButtonIcon(transform: String, paths: String*): TagOf[SVG] = {
    import japgolly.scalajs.react.vdom.svg_<^._
    <.svg(
      ^.width := "24px",
      ^.height := "24px",
      ^.color := "#000",
      ^.fill := "#000",
      ^.opacity := "0.35",
      ^.viewBox := "0 0 24 24",
      paths.toTagMod(path => <.path(^.transform := transform, ^.d := path))
    )
  }

  def makeButtonDiv(icon: TagOf[SVG]): TagOf[Div] = {
    import japgolly.scalajs.react.vdom.html_<^._
    <.div(
      ^.className := "tree-button-icon__div",
      icon
    )
  }

  val ellipsisIcon: TagOf[SVG] = makeButtonIcon(
    "translate(0, 0)",
    "M6 10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm12 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-6 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
  )

  val crossIcon: TagOf[SVG] = makeButtonIcon(
    "translate(0, 0)",
    "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
  )

  val handleIcon: TagOf[SVG] = {
    import japgolly.scalajs.react.vdom.svg_<^._
    <.svg(
      ^.width := "24px",
      ^.height := "24px",
      ^.color := "#000",
      ^.opacity := "0.35",
      ^.viewBox := "0 0 24 24",
      <.path(^.d := "M4 15h16v-2H4v2zm0 4h16v-2H4v2zm0-8h16V9H4v2zm0-6v2h16V5H4z")
    )
  }
}
