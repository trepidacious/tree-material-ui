package org.rebeam.tree.view.list

import japgolly.scalajs.react.vdom.html_<^._
//import org.rebeam.tree.view.View
import japgolly.scalajs.react._

object SortableView {

  import japgolly.scalajs.react.vdom.SvgTags._
  import japgolly.scalajs.react.vdom.SvgAttrs._

//  val HandleGrip =
//    ScalaComponent.builder[String]("HandleGrip")
//      .render_P(s =>
//        <.div(
//          ^.className := "react-sortable-handle",
//          svg(
//            ^.className := "react-sortable-handle-svg",
//            viewBox := "0 0 24 24",
//            path(d := "M4 15h16v-2H4v2zm0 4h16v-2H4v2zm0-8h16V9H4v2zm0-6v2h16V5H4z")
//          )
//        )
//      )
//      .build

//  val handle = SortableHandle.wrap(HandleGrip)("")

  val handle = ScalaComponent.static("HandleGrip")(
    <.div(
      ^.className := "react-sortable-handle",
      svg(
        ^.className := "react-sortable-handle-svg",
        viewBox := "0 0 24 24",
        path(d := "M4 15h16v-2H4v2zm0 4h16v-2H4v2zm0-8h16V9H4v2zm0-6v2h16V5H4z")
      )
    )
  )()
}
