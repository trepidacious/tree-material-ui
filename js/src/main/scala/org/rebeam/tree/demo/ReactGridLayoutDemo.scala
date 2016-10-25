package org.rebeam.tree.demo

import japgolly.scalajs.react.vdom.prefix_<^._
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import org.rebeam.tree.view.{ReactGridLayout, View}
import org.rebeam.tree.view.ReactGridLayout._

import scala.scalajs.js

object ReactGridLayoutDemo {

  class Backend(scope: BackendScope[Unit, Layout]) {

    val swapFirstTwo: Callback = {
      scope.modState(l => {
        val a = l(0)
        val b = l(1)

        val a2 = a.copy(i = b.i)
        val b2 = b.copy(i = a.i)

        List(a2, b2, l(2), l(3))
      })
    }

    def card(key: String) =
      <.div(^.key := key,
        MuiCard(style = js.Dynamic.literal("height" -> "100%"))(
//          MuiCardHeader(
//            title = "Card " + key : ReactNode,
//            subtitle = "Subtitle for card " + key : ReactNode
//          )(),
          MuiCardTitle(title = "Card " + key: ReactNode, subtitle = "Card subtitle": ReactNode)(),
          MuiCardText(style = js.Dynamic.literal("height" -> "100%"))("Text to display on card " + key),
          MuiCardActions(style = js.Dynamic.literal("position" -> "absolute", "bottom" -> "0px"))(
            MuiFlatButton(label = "Swap", onTouchTap = View.touch(swapFirstTwo))()
          )
        )
      )

    def render(props: Unit, l: Layout) = {
      ReactGridLayout(
        layout = l,
        cols = 2,
        rowHeight = 200,
        margin = XY(20, 20),
        containerPadding = XY(0, 0),
        onLayoutChange = newLayout => scope.modState(s => newLayout)
      )(
        List("a", "b", "c", "d").map(card)
      )
    }
  }

  val defaultLayout = List(
    LayoutItem("a", 0, 3),
    LayoutItem("b", 0, 2),
    LayoutItem("c", 0, 1),
    LayoutItem("d", 0, 0)
  )

  val c = ReactComponentB[Unit]("ReactGridLayoutDemo")
    .initialState(defaultLayout)
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
