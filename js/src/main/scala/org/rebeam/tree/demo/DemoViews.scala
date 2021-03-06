package org.rebeam.tree.demo

import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.DeltaIOContextSource
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import DemoData._
import japgolly.scalajs.react._
import org.rebeam.tree.view.pages.Breadcrumbs
import org.rebeam.tree.view.markdown.MarkdownView

object DemoViews {

  val streetView = cursorView[Street, Unit]("StreetView") { c =>
    <.div(
      <.p("Blah"),
      intView(c.zoom(Street.number).label("Number")),
      textView(c.zoom(Street.name).label("Name")),
      doubleView(c.zoom(Street.temperature).label("Temperature")),
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

  implicit val contextSource = DeltaIOContextSource.default

  implicit val rootSourceAddress = ServerRootComponent.noRootSource[Address]

  val addressView = ServerRootComponent[Address](noAddress, "api/address") {
    addressCursor => {
      val streetCursor = addressCursor.zoom(Address.street)
      <.div(
        <.h3("Address"),
        streetView(streetCursor)
      )
    }
  }

//  val stringView = ScalaFnComponent.apply((s: String) => <.span("stringView of " + s))

  val StringView =
    ScalaComponent.builder[String]("StringView")
      .render_P(s => <.div("StringView: ", s))
      .build


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
      ReactGridLayoutDemo.c(),
      WidthProvider.wrap(StringView)(WidthProvider.Props(true))("Props made it through!")
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

