package org.rebeam.tree.view

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.extra.Reusability

object View {
  def view[A](name: String, overlay: Boolean = true)(render: A => ReactElement) =
    ReactComponentB[A](name).render_P(render).build

  //We are careful to ensure that cursors remain equal if they are reusable
  implicit def cursorReuse[A]: Reusability[Cursor[A]] = Reusability.by_==
  implicit def labelledCursorReuse[A]: Reusability[LabelledCursor[A]] = Reusability.by_==

  def cursorView[A](name: String)(render: Cursor[A] => ReactElement) =
    ReactComponentB[Cursor[A]](name).render_P(render).configure(Reusability.shouldComponentUpdate).build

  def labelledCursorView[A](name: String, overlay: Boolean = false)(render: LabelledCursor[A] => ReactElement) =
    ReactComponentB[LabelledCursor[A]](name).render_P(render).configure(Reusability.shouldComponentUpdate).build

  def staticView(e: ReactElement, name: String = "StaticView") = ReactComponentB[Unit](name)
    .render(_ => e)
    .build

  def dynamicView(name: String)(render: => ReactElement) =
    ReactComponentB[Unit](name).render_P(_ => render).build

  val spinner = staticView(<.div(), "Spinner")

  val textView = labelledCursorView[String]("textView") { p =>
    <.form(
      <.div(
        <.input(
          ^.id := "string-view-input",
          ^.`type` := "text",
          ^.value := p.cursor.model,
          ^.onChange ==> ((e: ReactEventI) => p.cursor.set(e.target.value))
        ),
        <.label(
          ^.`for` := "string-view-input",
          p.label
        )
      )
    )
  }

  val textViewPlainLabel = labelledCursorView[String]("textViewPlainLabel") { p =>
    <.form(
      <.div(
        <.input(
          ^.id := "string-view-input",
          ^.`type` := "text",
          ^.value := p.cursor.model,
          ^.onChange ==> ((e: ReactEventI) => p.cursor.set(e.target.value))
        ),
        <.label(
          ^.`for` := "string-view-input",
          p.label
        )
      )
    )
  }

  val intView = labelledCursorView[Int]("intView") { p =>
    <.form(
      <.div(
        <.input(
          ^.id := "string-view-input",
          ^.`type` := "number",
          ^.value := p.cursor.model.toString,
          ^.onChange ==> ((e: ReactEventI) => p.cursor.set(e.target.value.toInt))
        ),
        <.label(
          ^.`for` := "string-view-input",
          p.label
        )
      )
    )
  }

  val booleanView = labelledCursorView[Boolean]("booleanView") { p =>
    <.form(
      <.label(
        <.input.checkbox(
          ^.checked := p.cursor.model,
          ^.onChange --> p.cursor.set(!p.cursor.model)
        ),
        <.span(
          p.label
        )
      )
    )
  }

  def actButton(s: String, c: Callback, accent: Boolean = false, colored: Boolean = true) =
    <.button(
      s,
      ^.onClick ==> ((e: ReactEventI) => e.preventDefaultCB >> c)
    )

}
