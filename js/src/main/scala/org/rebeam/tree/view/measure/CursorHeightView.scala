package org.rebeam.tree.view.measure

import japgolly.scalajs.react.ReactComponentC.ReqProps
import japgolly.scalajs.react._
import org.rebeam.tree.view._

object CursorHeightView {

  class Backend[A](scope: BackendScope[Cursor[A], Option[Double]])(renderCH: (Cursor[A], Option[Double]) => ReactElement) {
    def render(c: Cursor[A], height: Option[Double]): ReactComponentU_ = {
      Measure.height(
        d => scope.setState(Some(d.height))
      )(
        renderCH(c, height)
      )
    }
  }

  def apply[A](name: String)(render: (Cursor[A], Option[Double]) => ReactElement): ReqProps[Cursor[A], Option[Double], Backend[A], TopNode] = ReactComponentB[Cursor[A]](name)
    .initialState(None: Option[Double])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
