package org.rebeam.tree.view.icon

import japgolly.scalajs.react.vdom.ReactTagOf
import org.scalajs.dom.html.Div
import org.scalajs.dom.svg.SVG

object Icons {

  def makeWhiteIcon(transform: String, paths: String*): ReactTagOf[SVG] = {
    import japgolly.scalajs.react.vdom.svg.prefix_<^._
    <.svg(
      ^.width := "24px",
      ^.height := "24px",
      ^.color := "#FFF",
      ^.fill := "#FFF",
      ^.viewBox := "0 0 24 24",
      paths.map(path => <.path(^.transform := transform, ^.d := path))
    )
  }

  def makeButtonIcon(transform: String, paths: String*): ReactTagOf[SVG] = {
    import japgolly.scalajs.react.vdom.svg.prefix_<^._
    <.svg(
      ^.width := "24px",
      ^.height := "24px",
      ^.color := "#000",
      ^.fill := "#000",
      ^.opacity := "0.35",
      ^.viewBox := "0 0 24 24",
      paths.map(path => <.path(^.transform := transform, ^.d := path))
    )
  }

  def makeButtonDiv(icon: ReactTagOf[SVG]): ReactTagOf[Div] = {
    import japgolly.scalajs.react.vdom.prefix_<^._
    <.div(
      ^.className := "tree-button-icon__div",
      icon
    )
  }

  val ellipsisIcon: ReactTagOf[SVG] = makeButtonIcon(
    "translate(0, 0)",
    "M6 10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm12 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-6 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
  )

  val crossIcon: ReactTagOf[SVG] = makeButtonIcon(
    "translate(0, 0)",
    "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
  )

  val handleIcon: ReactTagOf[SVG] = {
    import japgolly.scalajs.react.vdom.svg.prefix_<^._
    <.svg(
      ^.width := "24px",
      ^.height := "24px",
      ^.color := "#000",
      ^.opacity := "0.35",
      ^.viewBox := "0 0 24 24",
      <.path(^.d := "M4 15h16v-2H4v2zm0 4h16v-2H4v2zm0-8h16V9H4v2zm0-6v2h16V5H4z")
    )
  }

  def makeArcHashIcon(hash: Int): ReactTagOf[SVG] = {
    import japgolly.scalajs.react.vdom.svg.prefix_<^._
    <.svg(
      ^.width := "24px",
      ^.height := "24px",
      ^.fill := "none",
      ^.stroke := "#fff",
      ^.strokeWidth := 2,
      ^.strokeLinecap := "round",
      ^.viewBox := "0 0 24 24",
      <.path(^.transform := "translate(0, 0)", ^.d := arcHashIconPath(24, hash))
    )
  }


  val cos60: Double = 0.5
  val sin60: Double = 0.866025403784439

  def arcDir(i: Int): (Double, Double) = i % 6 match {
    case 0 => (1, 0)
    case 1 => (cos60, -sin60)
    case 2 => (-cos60, -sin60)
    case 3 => (-1, 0)
    case 4 => (-cos60, sin60)
    case 5 => (cos60, sin60)
  }

  def arc(xc: Double, yc: Double, start: Int, end: Int, radius: Double): String = {
    val ds = arcDir(start)
    val xs = xc + radius * ds._1
    val ys = yc + radius * ds._2

    val de = arcDir(end)
    val xe = xc + radius * de._1
    val ye = yc + radius * de._2

    s"M $xs $ys A $radius $radius 0 0 0 $xe $ye"
  }

  def arcHashRingPath(c: Double, r: Double, hash: Int): String = {
    (0 until 6).map(
      i => if (((hash >> i) & 1) == 1) arc(c, c, i, i + 1, r) else ""
    ).mkString(" ")
  }

  def arcHashIconPath(size: Double, hash: Int): String = {
    val c = size / 2
    val r = size / 8
    (0 until 3).map(
      i => arcHashRingPath(c, r * (i + 1), hash >> (i * 3))
    ).mkString(" ")
  }

}
