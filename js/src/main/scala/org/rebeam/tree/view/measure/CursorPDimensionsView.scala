package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import org.rebeam.tree.view._
import org.rebeam.tree.view.measure.Measure.Dimensions

object CursorPDimensionsView {

  class Backend[A, P](scope: BackendScope[CursorP[A, P], Option[Dimensions]])(renderCPH: (CursorP[A, P], Option[Dimensions]) => ReactElement) {
    def render(cp: CursorP[A, P], height: Option[Dimensions]) = {
      Measure(
        whitelist = List("width", "height"),
        shouldMeasure = true,
        onMeasure = d => scope.setState(Some(d))
      )(
        renderCPH(cp, height)
      )
    }
  }

  def apply[A, P](name: String)(render: (CursorP[A, P], Option[Dimensions]) => ReactElement) = ReactComponentB[CursorP[A, P]](name)
    .initialState(None: Option[Dimensions])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
