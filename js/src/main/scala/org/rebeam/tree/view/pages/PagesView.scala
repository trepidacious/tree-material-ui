package org.rebeam.tree.view.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.view.Cursor
import org.rebeam.tree.view.transition.CSSTransitionGroup

object PagesView {

  case class State(direction: PagesTransition)

  val stateReuse: Reusability[State] = Reusability.byRefOr_==

  class Backend[M, P](scope: BackendScope[Cursor[M, Pages[P, P]], State])(renderToList: Cursor[M, Pages[P, P]] => List[(Key, VdomElement)])(transitions: PagesToTransition[P]) {

    def render(cp: Cursor[M, Pages[P, P]], state: State): VdomElement = {
      val panes = renderToList(cp)
      // We get an unavoidable extra div from the CSSTransitionGroup,
      // so we set a class to allow us to style it with flex etc. using CSS
      <.div(^.className := "tree-pages-view__outer-div")(
        CSSTransitionGroup(
          "tree-pages-view__page--transition-" + state.direction.className,
          appearTimeout = 550,
          leaveTimeout = 550,
          enterTimeout = 550,
          component = "div"
        )(
          <.div(
            ^.top :="0px",
            ^.left := "0px",
            ^.width := "100%",
            ^.height := "100%",
            ^.position := "absolute",
            ^.key := panes.last._1,
            panes.last._2
          )
        )
      )
    }
  }

  def apply[M, P](name: String)(renderToList: Cursor[M, Pages[P, P]] => List[(Key, VdomElement)])(implicit transitions: PagesToTransition[P]) = ScalaComponent.builder[Cursor[M, Pages[P, P]]](name)
    .initialState[State](State(PagesTransition.Left))
    .backend(new Backend[M, P](_)(renderToList)(transitions))
    .render(s => s.backend.render(s.props, s.state))
    .componentWillReceiveProps(
      scope => scope.setState(State(transitions(scope.currentProps.location.current, scope.nextProps.location.current)))
    )
    .configure(Reusability.shouldComponentUpdate(Cursor.cursorReusability, stateReuse))
    .build
}