package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import org.rebeam.tree.view._

object CursorPHeightView {

  class Backend[A, P](scope: BackendScope[CursorP[A, P], Option[Double]])(renderCPH: (CursorP[A, P], Option[Double]) => ReactElement) {
    def render(cp: CursorP[A, P], height: Option[Double]) = {
      Measure.height(
        d => scope.setState(Some(d.height))
      )(
        renderCPH(cp, height)
      )
    }
  }

  def apply[A, P](name: String)(render: (CursorP[A, P], Option[Double]) => ReactElement) = ReactComponentB[CursorP[A, P]](name)
    .initialState(None: Option[Double])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
