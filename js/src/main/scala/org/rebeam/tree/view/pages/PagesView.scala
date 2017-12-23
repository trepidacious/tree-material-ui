package org.rebeam.tree.view.pages

import japgolly.scalajs.react.Addons.ReactCssTransitionGroup
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactElement}
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.Delta
import org.rebeam.tree.view.Cursor

object PagesView {

  case class State(direction: PagesTransition)

  val stateReuse: Reusability[State] = Reusability.byRefOr_==

  class Backend[U, M, D <: Delta[U, M], P](scope: BackendScope[Cursor[U, M, D, Pages[P, P]], State])(renderToList: Cursor[U, M, D, Pages[P, P]] => List[ReactElement])(transitions: PagesToTransition[P]) {

    def render(cp: Cursor[U, M, D, Pages[P, P]], state: State): ReactElement = {
      val panes = renderToList(cp)
      // We get an unavoidable extra div from the ReactCssTransitionGroup,
      // so we set a class to allow us to style it with flex etc. using CSS
      <.div(^.className := "tree-pages-view__outer-div")(
        ReactCssTransitionGroup(
          "tree-pages-view__page--transition-" + state.direction.className,
          appearTimeout = 550,
          leaveTimeout = 550,
          enterTimeout = 550,
          component = "div")(
          <.div(
            ^.top :="0px",
            ^.left := "0px",
            ^.width := "100%",
            ^.height := "100%",
            ^.position := "absolute",
            ^.key := panes.last.key,
            panes.last
          )
        )
      )
    }
  }

  def apply[U, M, D <: Delta[U, M], P](name: String)(renderToList: Cursor[U, M, D, Pages[P, P]] => List[ReactElement])(implicit transitions: PagesToTransition[P]) = ReactComponentB[Cursor[U, M, D, Pages[P, P]]](name)
    .getInitialState[State](_=> State(PagesTransition.Left))
    .backend(new Backend[U, M, D, P](_)(renderToList)(transitions))
    .render(s => s.backend.render(s.props, s.state))
    .componentWillReceiveProps(
      scope => scope.$.setState(State(transitions(scope.currentProps.location.current, scope.nextProps.location.current)))
    )
    .configure(Reusability.shouldComponentUpdate(Cursor.cursorReusability, stateReuse))
    .build
}