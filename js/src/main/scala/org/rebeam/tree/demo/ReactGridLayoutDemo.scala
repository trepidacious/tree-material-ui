package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui.MuiPaper
import io.circe.Decoder
import japgolly.scalajs.react._
import org.rebeam.tree.view.ReactGridLayout
import org.rebeam.tree.view.ReactGridLayout._
import org.rebeam.tree.view.WSRootComponent.State

object ReactGridLayoutDemo {

  class Backend(scope: BackendScope[Unit, Layout]) {
    def render(props: Unit, l: Layout) = {
      println("Layout state: " + l)
      ReactGridLayout(
        layout = l,
        onLayoutChange = newLayout => scope.modState(s => newLayout)
      )(
        MuiPaper(key = "a")("A"),
        MuiPaper(key = "b")("B"),
        MuiPaper(key = "c")("C"),
        MuiPaper(key = "d")("D")
      )
    }
  }

  val defaultLayout = List(
    ReactGridLayout.LayoutItem("a", 0, 3),
    ReactGridLayout.LayoutItem("b", 0, 2),
    ReactGridLayout.LayoutItem("c", 0, 1),
    ReactGridLayout.LayoutItem("d", 0, 0)
  )

  val c = ReactComponentB[Unit]("ReactGridLayoutDemo")
    .initialState(defaultLayout)
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
