package org.rebeam.tree.view

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability

import scala.scalajs.js

object View {

  def touch(c: Callback): js.UndefOr[ReactTouchEventH => Callback] = {
    e: ReactTouchEventH => e.preventDefaultCB >> c
  }

  def view[A](name: String, overlay: Boolean = true)(render: A => ReactElement) =
    ReactComponentB[A](name).render_P(render).build

  //We are careful to ensure that cursors remain equal if they are reusable
  implicit def cursorReuse[A]: Reusability[Cursor[A]] = Reusability.by_==
  implicit def labelledCursorReuse[A]: Reusability[LabelledCursor[A]] = Reusability.by_==

  def cursorView[A](name: String)(render: Cursor[A] => ReactElement) =
    ReactComponentB[Cursor[A]](name).render_P(render).configure(Reusability.shouldComponentUpdate).build

  def labelledCursorView[A](name: String)(render: LabelledCursor[A] => ReactElement) =
    ReactComponentB[LabelledCursor[A]](name).render_P(render).configure(Reusability.shouldComponentUpdate).build

  def staticView(name: String)(e: ReactElement) = ReactComponentB[Unit](name)
    .render(_ => e)
    .build

  def dynamicView(name: String)(render: => ReactElement) =
    ReactComponentB[Unit](name).render_P(_ => render).build

  val spinner = staticView("Spinner")(
    MuiCircularProgress(mode = DeterminateIndeterminate.indeterminate)()
  )

  val textView = labelledCursorView[String]("textView") { p =>
    MuiTextField(
      value = p.cursor.model,
      onChange = (e: ReactEventI) => e.preventDefaultCB >>  p.cursor.set(e.target.value),
      floatingLabelText = p.label: ReactNode
    )()
  }

  val textViewPlainLabel = labelledCursorView[String]("textViewPlainLabel") { p =>
    MuiTextField(
      value = p.cursor.model,
      onChange = (e: ReactEventI) => e.preventDefaultCB >>  p.cursor.set(e.target.value),
      hintText = p.label: ReactNode
    )()
  }

  val intView = labelledCursorView[Int]("intView") { p =>
    MuiTextField(
      `type` = "number",
      value = p.cursor.model.toString,
      onChange = (e: ReactEventI) => e.preventDefaultCB >> p.cursor.set(e.target.value.toInt),
      floatingLabelText = p.label: ReactNode
    )()
  }

  val booleanView = labelledCursorView[Boolean]("booleanView") { p =>
    MuiCheckbox(
      label = p.label: ReactNode,
      checked = p.cursor.model,
      onCheck = (e: ReactEventH, b: Boolean) => e.preventDefaultCB >> p.cursor.set(!p.cursor.model)
    )()
  }

  val booleanViewUnlabelled = cursorView[Boolean]("booleanView") { p =>
    MuiCheckbox(
      checked = p.model,
      onCheck = (e: ReactEventH, b: Boolean) => e.preventDefaultCB >> p.set(!p.model)
    )()
  }

  def raisedButton(label: String, primary: Boolean = false, secondary: Boolean = false)(callback: Callback) = {
    MuiRaisedButton(
      label = label,
      primary = primary,
      secondary = secondary,
      onTouchTap = touch(callback)
    )()
  }

}
