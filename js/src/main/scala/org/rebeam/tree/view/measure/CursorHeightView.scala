package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import org.rebeam.tree.view._
import japgolly.scalajs.react.vdom.html_<^._

object CursorHeightView {

  class Backend[A, P](scope: BackendScope[Cursor[A, P], Option[Double]])(renderCPH: (Cursor[A, P], Option[Double]) => VdomElement) {
    def render(cp: Cursor[A, P], height: Option[Double]) = {
      Measure.height(
        d => scope.setState(Some(d.height))
      )(
        renderCPH(cp, height)
      )
    }
  }

  def apply[A, P](name: String)(render: (Cursor[A, P], Option[Double]) => VdomElement) = ScalaComponent.builder[Cursor[A, P]](name)
    .initialState(None: Option[Double])
    .backend(new Backend(_)(render))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
