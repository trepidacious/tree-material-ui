package org.rebeam.tree.demo

import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.{DeltaIOContextSource, Moment, ValueDelta}
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.Cursor._
import DemoData._
import chandu0101.scalajs.react.components.materialui._
import io.circe.Encoder
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import org.rebeam.tree.ValueDelta.StringValueDelta
import org.rebeam.tree.view.View.AsStringView.Backend
//import org.rebeam.tree.demo.DemoData.Priority._
import org.rebeam.tree.demo.DemoRoutes._
import org.rebeam.tree.view.infinite.Infinite
import org.rebeam.tree.view.pages.{Breadcrumbs, Pages}
import org.rebeam.tree.view.list.{SortableContainer, SortableElement, SortableListItem}
import Pages._
import org.rebeam.tree.view.markdown.MarkdownView

import org.scalajs.dom._

object DemoViews {

  val iv = intView[Unit]
  val tv = textView[Unit]
  val dv = doubleView[Unit]

//  val PlainStringView = ReactComponentB[String]("StringViewThing")
//    .render_P(s => <.div(s): ReactElement)
//    .configure(Reusability.shouldComponentUpdateWithOverlay)
//    .componentWillUnmount(_ => Callback{println("StringViewThing unmounted! Again! FFS!")})
//    .build

  val streetView = cursorView[Unit, Street, StreetDelta, Unit]("StreetView") { c =>
    <.div(
//      ^.paddingTop := "20px",
      iv(c.zoom(StreetDelta.number).label("Number")),
      tv(c.zoom(StreetDelta.name).label("Name")),
      dv(c.zoom(StreetDelta.temperature).label("Temperature")),
      raisedButton("Number multiple", primary = true){
        c.act(StreetDelta.NumberMultiple(10))
      },
      raisedButton("Capitalise", secondary = true){
        c.act(StreetDelta.Capitalise)
      }
    )
  }

  val noAddress = <.div(
    <.h3("Address"),
    spinner()
  )

  implicit val contextSource = DeltaIOContextSource.default

  implicit val rootSourceAddress = ServerRootComponent.noRootSource[Unit, Address, AddressDelta]

  val addressView = ServerRootComponent[Unit, Address, AddressDelta](noAddress, "api/address") {
    addressCursor => {
      val streetCursor = addressCursor.zoom(AddressDelta.street)
      <.div(
        <.h3("Address"),
        streetView(streetCursor)
      )
    }
  }

  val homeView = staticView("home") {
    val title = Breadcrumbs.container(
      labelHero("Home")
    )

    val contents = <.div(
      ^.margin := "24px",
      MarkdownView(
      """
        |# Introduction
        |This should render as markdown
        |# Lists
        |1. First
        |2. Second
        |3. Third
        |## Unordered
        |* A
        |* B
        |* C
        |# Formatting
        |**Bold**
        |*Italic*
        |# HTML
        |<span>This should appear with escaped HTML span tags</span>
      """.stripMargin),
      ReactGridLayoutDemo.c()
    )

    <.div(
      ^.width := "100%",
      ^.height := "100%",
      ^.position := "absolute",
      ^.top := "0px",
      ^.bottom := "0px",
      PageLayout(MaterialColor.BlueGrey(500), 128, "", None, Some(title), Some(contents), scrollContents = true)
    )
  }

}

