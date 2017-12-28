package org.rebeam.tree.demo

import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import DemoData._
import japgolly.scalajs.react._
import org.rebeam.tree.view.pages._
import org.rebeam.tree.view.markdown.MarkdownView

object DemoViews {

  val streetView = cursorView[Nothing, Street, StreetDelta, Unit]("StreetView") { c =>
    <.div(
      intView(c.zoom(StreetDelta.number).label("Number")),
      textView(c.zoom(StreetDelta.name).label("Name")),
      doubleView(c.zoom(StreetDelta.temperature).label("Temperature")),
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

  implicit val rootSourceAddress = ServerRootComponent.noRootSource[Nothing, Address, AddressDelta]

  val addressView = ServerRootComponent[Nothing, Address, AddressDelta](noAddress, "api/address") {
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

