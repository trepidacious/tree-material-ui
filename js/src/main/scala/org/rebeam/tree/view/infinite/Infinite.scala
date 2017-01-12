package org.rebeam.tree.view.infinite

import chandu0101.macros.tojs.JSMacro
import japgolly.scalajs.react._
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichGenTraversableOnce

case class Infinite(handleScroll: js.UndefOr[HTMLElement => Callback] = js.undefined,
                         preloadAdditionalHeight: js.UndefOr[Int] = js.undefined,
                         isInfiniteLoading: js.UndefOr[Boolean] = js.undefined,
                         preloadBatchSize: js.UndefOr[Int] = js.undefined,
                         containerHeight: js.UndefOr[Int] = js.undefined,
                         ref: js.UndefOr[InfiniteM => Unit] = js.undefined,
                         loadingSpinnerDelegate: js.UndefOr[ReactElement] = js.undefined,
                         timeScrollStateLastsForAfterUserScrolls: js.UndefOr[Int] = js.undefined,
                         elementHeight: Double,
                         key: js.UndefOr[String] = js.undefined,
                         className: js.UndefOr[String] = js.undefined,
                         infiniteLoadBeginBottomOffset: js.UndefOr[Int] = js.undefined,
                         onInfiniteLoad: js.UndefOr[Callback] = js.undefined,
                         useWindowAsScrollContainer: js.UndefOr[Boolean] = js.undefined) {

  def apply(children: Seq[ReactElement]) = {
    val props = JSMacro[Infinite](this)
    val f = React.asInstanceOf[js.Dynamic].createFactory(js.Dynamic.global.Infinite)
    f(props, children.toJSArray).asInstanceOf[ReactComponentU_]
  }
}

@js.native
trait InfiniteM extends js.Object {
  def getScrollTop(): Double = js.native
}
