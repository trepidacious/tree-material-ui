package org.rebeam.tree.demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view._
import org.rebeam.tree.view.sortable._

object WidthProviderDemo {

  case class HelloData(firstName: String, lastName: String)

  val helloView = ReactComponentB[HelloData]("helloView")
    .render(d => <.div(s"Hello ${d.props.firstName} ${d.props.lastName}!"))
    .build

  val widthHelloView = WidthProvider(measureBeforeMount = true)(HelloData("Higher", "OrderComponent"))(helloView)

}
