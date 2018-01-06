package org.rebeam.tree.view.transition

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomNode

import scala.scalajs.js
import scala.scalajs.js.|

/**
  * See https://github.com/reactjs/react-transition-group/tree/v1-stable#high-level-api-csstransitiongroup
  */
object CSSTransitionGroup {

  @js.native
  trait Props extends js.Object {
    var transitionName          : js.UndefOr[String | GroupNames]
    var transitionAppear        : js.UndefOr[Boolean]
    var transitionEnter         : js.UndefOr[Boolean]
    var transitionLeave         : js.UndefOr[Boolean]
    var transitionAppearTimeout : js.UndefOr[Int]
    var transitionEnterTimeout  : js.UndefOr[Int]
    var transitionLeaveTimeout  : js.UndefOr[Int]
    // component name for the TransitionGroup itself
    var component               : js.UndefOr[String]
    // class for the TransitionGroup itself
    var className               : js.UndefOr[String]
  }

  @js.native
  trait GroupNames extends js.Object {
    var appear        : js.UndefOr[String]
    var enter         : js.UndefOr[String]
    var leave         : js.UndefOr[String]
    var appearActive  : js.UndefOr[String]
    var enterActive   : js.UndefOr[String]
    var leaveActive   : js.UndefOr[String]
  }

  private val rawGroupComponent = js.Dynamic.global.CSSTransitionGroup
  private val groupComponent = JsComponent[Props, Children.Varargs, Null](rawGroupComponent)

  /**
    * [[CSSTransitionGroup]] is based on `ReactTransitionGroup` and is an easy way to perform CSS transitions and
    * animations when a React component enters or leaves the DOM.
    *
    * @param name The prefix for all class-names that will be applied to elements to trigger animations.
    * @param appear Enable/disable animating appear animations.
    * @param enter Enable/disable animating enter animations.
    * @param leave Enable/disable animating leave animations.
    * @param appearTimeout Timeout in milliseconds.
    * @param enterTimeout Timeout in milliseconds.
    * @param leaveTimeout Timeout in milliseconds.
    * @param component The container type around all child elements. By default this renders as a span.
    * @param className Class name to apply to the container.
    * @param children You must provide the key attribute for all children of [[CSSTransitionGroup]],
    *                 even when only rendering a single item. This is how React will determine which
    *                 children have entered, left, or stayed.
    *                 @see https://facebook.github.io/react/docs/animation.html
    */
  def apply(
    name         : js.UndefOr[String | GroupNames],
    appear       : js.UndefOr[Boolean] = js.undefined,
    enter        : js.UndefOr[Boolean] = js.undefined,
    leave        : js.UndefOr[Boolean] = js.undefined,
    appearTimeout: js.UndefOr[Int]     = js.undefined,
    enterTimeout : js.UndefOr[Int]     = js.undefined,
    leaveTimeout : js.UndefOr[Int]     = js.undefined,
    component    : js.UndefOr[String]  = js.undefined,
    className    : js.UndefOr[String]  = js.undefined
  )(children: VdomNode*): JsComponent.Unmounted[Props, Null] = {

    val p = (new js.Object).asInstanceOf[Props]
    p.transitionName = name
    p.transitionAppear = appear
    p.transitionEnter = enter
    p.transitionLeave = leave
    p.transitionAppearTimeout = appearTimeout
    p.transitionEnterTimeout = enterTimeout
    p.transitionLeaveTimeout = leaveTimeout
    p.component = component
    p.className = className

    groupComponent.apply(p)(children:_*)
  }

}

