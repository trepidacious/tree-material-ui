package org.rebeam.tree.demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view._
import org.rebeam.tree.view.sortable._

object WidthProviderDemo {

  val helloView = ReactComponentB[Unit]("helloView")
    .render(_ => <.div("Hello!"))
    .build

  val widthHelloView = WidthProvider(measureBeforeMount = true)(helloView)

}
