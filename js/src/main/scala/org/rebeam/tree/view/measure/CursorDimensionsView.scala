package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import org.rebeam.tree.Delta
import org.rebeam.tree.view._
import org.rebeam.tree.view.measure.Measure.Dimensions

object CursorDimensionsView {

  class Backend[U, A, D <: Delta[U, A], P](scope: BackendScope[Cursor[U, A, D, P], Option[Dimensions]])(renderCPH: (Cursor[U, A, D, P], Option[Dimensions]) => ReactElement) {
    def render(cp: Cursor[U, A, D, P], height: Option[Dimensions]): ReactComponentU_ = {
      Measure(
        whitelist = List("width", "height"),
        shouldMeasure = true,
        onMeasure = d => scope.setState(Some(d))
      )(
        renderCPH(cp, height)
      )
    }
  }

  def apply[U, A, D <: Delta[U, A], P](name: String)(render: (Cursor[U, A, D, P], Option[Dimensions]) => ReactElement) = ReactComponentB[Cursor[U, A, D, P]](name)
    .initialState(None: Option[Dimensions])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
