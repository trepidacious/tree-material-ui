package org.rebeam.tree.view.sortable

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, ReactElement}

object SortableListItem {

  case class Props(leftIcon: ReactElement, content: ReactElement, onClick: Callback, onClickContents: Callback)

  val component = ReactComponentB[Props]("SortableListItem")
    .render_P(p => {

      <.div(
        ^.onClick --> p.onClick,
        ^.cursor := "pointer",
        ^.className := "react-sortable-item",
        ^.display := "flex",
        <.div(
          ^.flex := "0 0 56px",
          p.leftIcon
        ),
        <.div(
          ^.onClick --> p.onClickContents,
          ^.flex := "1 1 auto",
          p.content
        ),
        <.div(
          ^.flex := "0 0 56px",
          SortableView.handle
        )
      )

    })
    .build

  def apply(leftIcon: ReactElement, content: ReactElement, onClick: Callback = Callback.empty, onClickContents: Callback = Callback.empty) =
    component(Props(leftIcon, content, onClick, onClickContents))

  def twoLines(line1: String, line2: String) =
    <.div(
      <.span(line1),
      <.br,
      <.span(
        ^.color := "rgba(0, 0, 0, 0.541176)",
        line2
      )
    )
}
