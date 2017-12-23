package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import org.rebeam.tree.Delta
import org.rebeam.tree.view._

object CursorHeightView {

  class Backend[U, A, D <: Delta[U, A], P](scope: BackendScope[Cursor[U, A, D, P], Option[Double]])(renderCPH: (Cursor[U, A, D, P], Option[Double]) => ReactElement) {
    def render(cp: Cursor[U, A, D, P], height: Option[Double]) = {
      Measure.height(
        d => scope.setState(Some(d.height))
      )(
        renderCPH(cp, height)
      )
    }
  }

  def apply[U, A, D <: Delta[U, A], P](name: String)(render: (Cursor[U, A, D, P], Option[Double]) => ReactElement) = ReactComponentB[Cursor[U, A, D, P]](name)
    .initialState(None: Option[Double])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
