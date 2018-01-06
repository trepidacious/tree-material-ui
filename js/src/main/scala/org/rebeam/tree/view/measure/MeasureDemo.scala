package org.rebeam.tree.view.measure

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import org.rebeam.tree.view.measure.Measure.Dimensions

object MeasureDemo {

  class Backend(scope: BackendScope[Unit, Dimensions]) {
    def render(props: Unit, l: Dimensions) = {
      Measure(
        shouldMeasure = true,
        whitelist = List("width", "height"),
        onMeasure = d => scope.setState(d)
      )(
        <.div(
          ^.width := "100%",
          ^.height := "100%",
          s"${l.width} x ${l.height}"
        )
      )
    }
  }

  val c = ScalaComponent.builder[Unit]("MeasureDemo")
    .initialState(Dimensions(0, 0))
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
