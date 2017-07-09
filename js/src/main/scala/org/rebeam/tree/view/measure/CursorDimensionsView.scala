package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import org.rebeam.tree.view._
import org.rebeam.tree.view.measure.Measure.Dimensions

object CursorDimensionsView {

  class Backend[A, P](scope: BackendScope[Cursor[A, P], Option[Dimensions]])(renderCPH: (Cursor[A, P], Option[Dimensions]) => ReactElement) {
    def render(cp: Cursor[A, P], height: Option[Dimensions]): ReactComponentU_ = {
      Measure(
        whitelist = List("width", "height"),
        shouldMeasure = true,
        onMeasure = d => scope.setState(Some(d))
      )(
        renderCPH(cp, height)
      )
    }
  }

  def apply[A, P](name: String)(render: (Cursor[A, P], Option[Dimensions]) => ReactElement) = ReactComponentB[Cursor[A, P]](name)
    .initialState(None: Option[Dimensions])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
