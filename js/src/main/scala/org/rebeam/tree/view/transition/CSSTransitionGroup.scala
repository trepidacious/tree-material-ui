package org.rebeam.tree.view.transition

import japgolly.scalajs.react.vdom.ReactAttr.Generic
import japgolly.scalajs.react.{JsComponentType, React, ReactComponentU_, ReactNode, TopNode, vdom}

import scala.scalajs.js

object CSSTransitionGroup {
  /** Items in the CSSTransitionGroup need this attribute for animation to work properly. */
  @inline final def key: Generic = vdom.Attrs.key

  private val factory =
    React.createFactory(
      React.addons.CSSTransitionGroup.asInstanceOf[JsComponentType[js.Any, js.Any, TopNode]])
}

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
  * @param ref Reference for this component.
  * @param className Class name to apply to the container.
  * @see https://facebook.github.io/react/docs/animation.html
  */
case class CSSTransitionGroup(name         : String,
                              appear       : js.UndefOr[Boolean] = js.undefined,
                              enter        : js.UndefOr[Boolean] = js.undefined,
                              leave        : js.UndefOr[Boolean] = js.undefined,
                              appearTimeout: js.UndefOr[Int]     = js.undefined,
                              enterTimeout : js.UndefOr[Int]     = js.undefined,
                              leaveTimeout : js.UndefOr[Int]     = js.undefined,
                              component    : js.UndefOr[String]  = js.undefined,
                              ref          : js.UndefOr[String]  = js.undefined,
                              className    : js.UndefOr[String]  = js.undefined) {
  def toJs: js.Object = {
    val o = js.Dictionary.empty[js.Any]
    o("transitionName") = name
    appear        foreach (o("transitionAppear"       ) = _)
    enter         foreach (o("transitionEnter"        ) = _)
    leave         foreach (o("transitionLeave"        ) = _)
    appearTimeout foreach (o("transitionAppearTimeout") = _)
    enterTimeout  foreach (o("transitionEnterTimeout" ) = _)
    leaveTimeout  foreach (o("transitionLeaveTimeout" ) = _)
    component     foreach (o("component"              ) = _)
    ref           foreach (o("ref"                    ) = _)
    className     foreach (o("className"              ) = _)
    o.asInstanceOf[js.Object]
  }

  /**
    * You must provide the key attribute for all children of [[CSSTransitionGroup]], even when only rendering a
    * single item. This is how React will determine which children have entered, left, or stayed.
    */
  def apply(children: ReactNode*): ReactComponentU_ =
    CSSTransitionGroup.factory(toJs, children.toJsArray).asInstanceOf[ReactComponentU_]
}
