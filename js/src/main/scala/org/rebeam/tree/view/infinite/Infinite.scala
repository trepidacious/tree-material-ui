package org.rebeam.tree.view.infinite

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js

object Infinite {

  @js.native
  trait Props extends js.Object {
    var containerHeight: Int = js.native
    var elementHeight: Double = js.native
    var key: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
    var useWindowAsScrollContainer: js.UndefOr[Boolean] = js.native
  }

  private val rawComponent = js.Dynamic.global.Infinite
  private val component = JsComponent[Props, Children.Varargs, Null](rawComponent)

  def apply(containerHeight: Int,
            elementHeight: Double,
            key: js.UndefOr[String] = js.undefined,
            className: js.UndefOr[String] = js.undefined,
            useWindowAsScrollContainer: js.UndefOr[Boolean] = js.undefined)
            (children: Seq[VdomElement]) = {

    val p = (new js.Object).asInstanceOf[Props]
    p.containerHeight = containerHeight
    p.elementHeight = elementHeight
    p.key = key
    p.className = className
    p.useWindowAsScrollContainer = useWindowAsScrollContainer

    component(p)(children: _*)
  }

}

