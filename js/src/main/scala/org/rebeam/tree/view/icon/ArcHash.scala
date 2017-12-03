package org.rebeam.tree.view.icon

import japgolly.scalajs.react.vdom.ReactTagOf
import org.rebeam.tree.sync.{Guid, Id, Ref}
import org.rebeam.tree.util.CRC32
import org.rebeam.tree.view.{Color, MaterialColor}
import org.scalajs.dom.svg.SVG

trait ArcHashable[A] {
  def arcHash(a: A): Int
}

object ArcHashable {

  def create[A](f: A => Int): ArcHashable[A] = new ArcHashable[A] {
    def arcHash(a: A): Int = f(a)
  }

  def hashGuid(guid: Guid): Int = CRC32.empty
    .updateLong(guid.clientId.id)
    .updateLong(guid.clientDeltaId.id)
    .updateLong(guid.withinDeltaId.id).value

  implicit val arcHashableGuid: ArcHashable[Guid] = create(hashGuid)
  implicit val arcHashableId: ArcHashable[Id[_]] = create(id => hashGuid(id.guid))
  implicit val arcHashableRef: ArcHashable[Ref[_]] = create(ref => hashGuid(ref.id.guid))
  implicit val arcHashableString: ArcHashable[String] = create(s => CRC32(s.getBytes("utf-8")).value)

}

object ArcHash {

  val backgrounds: Seq[Color] = Seq(
    MaterialColor.Red(600),
    MaterialColor.Pink(500),
    MaterialColor.Purple(500),
    MaterialColor.DeepPurple(500),
    MaterialColor.Indigo(500),
    MaterialColor.Blue(500),
    MaterialColor.LightBlue(300),
    MaterialColor.Cyan(500),
    MaterialColor.Teal(500),
    MaterialColor.Green(600),
    MaterialColor.LightGreen(600),
    MaterialColor.Lime(600),
    MaterialColor.Yellow(700),
    MaterialColor.Amber(500),
    MaterialColor.Orange(500),
    MaterialColor.DeepOrange(500)
  )

  def backgroundForIndex(i: Int) = backgrounds(i % backgrounds.size)

  def color[A](a: A)(implicit ah: ArcHashable[A]): Color = color(ah.arcHash(a))

  def color(hash: Int): Color = {
    backgroundForIndex((hash >> 24) & 0xFF)
  }

  def icon[A](a: A)(implicit ah: ArcHashable[A]): ReactTagOf[SVG] = icon(ah.arcHash(a))

  def icon(hash: Int): ReactTagOf[SVG] = {
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

    s"M$xs,$ys A$radius,$radius,0,0,0,$xe,$ye"
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
