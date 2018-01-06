//package org.rebeam.tree.demo
//
//import japgolly.scalajs.react._
//import japgolly.scalajs.react.vdom.html_<^._
//import org.rebeam.tree.view._
//import org.rebeam.tree.view.list._
//
//object WidthProviderDemo {
//
//  case class HelloData(firstName: String, lastName: String)
//
//  val helloView = ScalaComponent.builder[HelloData]("helloView")
//    .render(d => <.div(s"Hello ${d.props.firstName} ${d.props.lastName}!"))
//    .build
//
////  val widthHelloView = WidthProvider(measureBeforeMount = true)(HelloData("Higher", "OrderComponent"))(helloView)
//
//  val widthHelloView = WidthProvider.wrap(helloView)
//
//  val widthBeforeMountHelloView = widthHelloView(WidthProvider.Props(true))
//}
