package org.rebeam.tree.view.infinite

import chandu0101.macros.tojs.JSMacro
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js

case class Infinite(handleScroll: js.UndefOr[HTMLElement => Callback] = js.undefined,
                         preloadAdditionalHeight: js.UndefOr[Int] = js.undefined,
                         isInfiniteLoading: js.UndefOr[Boolean] = js.undefined,
                         preloadBatchSize: js.UndefOr[Int] = js.undefined,
                         containerHeight: Int,
                         ref: js.UndefOr[InfiniteM => Unit] = js.undefined,
                         loadingSpinnerDelegate: js.UndefOr[VdomElement] = js.undefined,
                         timeScrollStateLastsForAfterUserScrolls: js.UndefOr[Int] = js.undefined,
                         elementHeight: Double,
                         key: js.UndefOr[String] = js.undefined,
                         className: js.UndefOr[String] = js.undefined,
                         infiniteLoadBeginBottomOffset: js.UndefOr[Int] = js.undefined,
                         onInfiniteLoad: js.UndefOr[Callback] = js.undefined,
                         useWindowAsScrollContainer: js.UndefOr[Boolean] = js.undefined) {

  def apply(children: Seq[VdomElement]) = {
    val props     = JSMacro[Infinite](this)
    val component = JsComponent[js.Object, Children.Varargs, Null](js.Dynamic.global.Infinite)
    component(props)(children: _*)
  }
}

@js.native
trait InfiniteM extends js.Object {
  def getScrollTop(): Double = js.native
}
