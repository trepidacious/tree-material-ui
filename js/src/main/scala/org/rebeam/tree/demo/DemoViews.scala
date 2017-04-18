package org.rebeam.tree.demo

import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.Moment
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.Cursor._
import DemoData._
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import org.rebeam.tree.demo.DemoData.Priority._
import org.rebeam.tree.demo.DemoRoutes._
import org.rebeam.tree.view.infinite.Infinite
import org.rebeam.tree.view.pages.{Breadcrumbs, Pages}
import org.rebeam.tree.view.list.{SortableContainer, SortableElement, SortableListItem}
import Pages._

import scala.scalajs.js

object DemoViews {

  val streetView = cursorView[Street]("StreetView") { c =>
    <.div(
      <.p("Blah"),
      intView(c.zoomN(Street.number).label("Number")),
      textView(c.zoomN(Street.name).label("Name")),
      doubleView(c.zoomN(Street.temperature).label("Temperature")),
      raisedButton("Number multiple", primary = true){
        c.act(StreetAction.NumberMultiple(10): StreetAction)
      },
      raisedButton("Capitalise", secondary = true){
        c.act(StreetAction.Capitalise: StreetAction)
      }
    )
  }

  val noAddress = <.div(
    <.h3("Address"),
    spinner()
  )

  val addressView = ServerRootComponent[Address](noAddress, "api/address") {
    addressCursor => {
      val streetCursor = addressCursor.zoomN(Address.street)
      <.div(
        <.h3("Address"),
        streetView(streetCursor)
      )
    }
  }


  val homeView = staticView("home")(
    <.div (
      ^.margin := "24px",
      <.h3("Home"),
//      ReactGridLayoutDemo.c()
      SortableContainerDemo.c()
//        WidthProviderDemo.widthBeforeMountHelloView(
//          WidthProviderDemo.HelloData("Higher", "Order Component")
//        )
    )
  )

}

