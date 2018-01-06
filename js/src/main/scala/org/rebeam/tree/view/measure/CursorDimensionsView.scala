package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import org.rebeam.tree.view._
import org.rebeam.tree.view.measure.Measure.Dimensions
import japgolly.scalajs.react.vdom.html_<^._

object CursorDimensionsView {

  class Backend[A, P](scope: BackendScope[Cursor[A, P], Option[Dimensions]])(renderCPH: (Cursor[A, P], Option[Dimensions]) => VdomElement) {
    def render(cp: Cursor[A, P], height: Option[Dimensions]) = {
      Measure(
        whitelist = List("width", "height"),
        shouldMeasure = true,
        onMeasure = d => scope.setState(Some(d))
      )(
        renderCPH(cp, height)
      )
    }
  }

  def apply[A, P](name: String)(render: (Cursor[A, P], Option[Dimensions]) => VdomElement) = ScalaComponent.builder[Cursor[A, P]](name)
    .initialState(None: Option[Dimensions])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
