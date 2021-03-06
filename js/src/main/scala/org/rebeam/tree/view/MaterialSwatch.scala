package org.rebeam.tree.view

import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.view.MaterialColor._
import org.rebeam.tree.view.View._

object MaterialSwatch {

  val shades: Seq[Int] = Seq(50, 100, 200, 300, 400, 500, 600, 700, 800, 900)

  def swatches(name: String, colors: Seq[(String, Color)]) = {
    <.ul(
      colors.toTagMod (c => {
        val l = c._2.toHSLA.l
        <.li(
          name + " " + c._1,
          ^.color := (if (l < 0.6) Color.White else Color.Black).toString,
          ^.backgroundColor := c._2.toString()
        )
      })
    )
  }

  val SingleFamilyView = view[Family]("FamilyView") {
    f => swatches(f.name, Seq(("", f())))
  }

  val ShadedFamilyView = view[ShadedFamily]("FamilyView") {
    f => swatches(f.name, shades.map(shade => (shade.toString, f(shade))))
  }

  val AccentedFamilyView = view[AccentedFamily]("FamilyView") {
    f => swatches(
      f.name,
      Seq(
        shades.map(shade => (shade.toString, f(shade))),
        Seq(
          ("A100", f.a100),
          ("A200", f.a200),
          ("A400", f.a400),
          ("A700", f.a700)
        )
      ).flatten
    )
  }

  val AllFamiliesView = staticView("AllFamiliesView"){
    <.div(
      accented.toTagMod(f => AccentedFamilyView(f)),
      shaded.toTagMod(f => ShadedFamilyView(f)),
      single.toTagMod(f => SingleFamilyView(f))
    )
  }

}
