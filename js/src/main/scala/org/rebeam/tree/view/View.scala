package org.rebeam.tree.view

import org.rebeam.tree.TreeUtils._
import chandu0101.scalajs.react.components.Implicits._
import chandu0101.scalajs.react.components.materialui._
import io.circe.Encoder
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


  trait StringCodec[A] {
    def format(a: A): String
    //TODO make into an Xor with error to display as validation
    def parse(s: String): Option[A]
  }

  /**
    * This allows for editing of values that can be represented as Strings.
    * We use a codec that provides a mapping from some strings to model values, and
    * from any model value to a string.
    *
    * We tolerate the case where there are multiple strings mapping to the same model
    * value, and in this case we retain the user's version until the model value changes.
    * So for example if the model values are Doubles, we permit "1." to remain in the input
    * even if the canonical formatting of 1.0 is "1". Another example would be retaining the
    * user's choice of case for case-insensitive input.
    *
    * In addition, we allow the user to edit invalid strings that do not map to a model
    * value, in case they wish to edit (type) in a sequence that reaches a valid string via
    * invalid ones.
    * So for example if the model values are Doubles, we permit the user to go from "1.11" to
    * "11.1" by first inserting an additional "." to give "1.1.1" then removing the first "." - for
    * some people this might be the natural approach, and we shouldn't interfere.
    *
    * Whenever a new model value is received via props, it is used to replace the currently editing
    * value, unless the currently editing value would parse to the same value.
    *
    * To edit doubles properly we need to maintain state. The conversion from String to Double is many to
    * one (for example "1", "1." and "1.0" all map to 1d, and back to say "1"). This makes it impossible to
    * type "1.0" when every change to the string contents of the input is converted to a Double and back -
    * we get stuck trying to type the "." and having it removed.
    *
    * Therefore we keep the actual typed value in the input as a string in our state, and use this to set
    * the cursor to the corresponding double. When the cursor model changes, we use the state by preference,
    * and only change it to the model's string representation if the state does NOT parse to the same double
    * as the cursor model (i.e. when the model changes in a way that is not consistent with the state).
    */
  object AsStringView {

    class Backend[A](scope: BackendScope[LabelledCursor[A], String])(implicit codec: StringCodec[A], encoder: Encoder[A]) {

      def render(props: LabelledCursor[A], state: String) = {
        MuiTextField(
          value = state,
          onChange = (e: ReactEventI) => {
            val input = e.target.value
            val parsed = codec.parse(input)
            parsed match {
              // If we have a parsed value, and it is different to cursor's model, then set model
              case Some(d) if d != props.cursor.model => e.preventDefaultCB >> props.cursor.set(d)

              // Otherwise just change state - we are editing without producing a valid new value,
              // but we may be on the way to a valid new value
              case _ => e.preventDefaultCB >> scope.setState(input)
            }
          },
          floatingLabelText = props.label: ReactNode
        )()
      }
    }

    def component[A](name: String, codec: StringCodec[A])(implicit encoder: Encoder[A]) = ReactComponentB[LabelledCursor[A]](name)
      .getInitialState[String](scope => scope.props.cursor.model.toString)
      .backend(new Backend[A](_)(codec, encoder))
      .render(s => s.backend.render(s.props, s.state))
      .componentWillReceiveProps(
        scope => {
          println("Will receive props " + scope.nextProps.cursor.model + " with state " + scope.currentState)
          val nextModel = scope.nextProps.cursor.model
          val currentModel = scope.currentProps.cursor.model
          if (nextModel != currentModel && !codec.parse(scope.currentState).contains(nextModel)) {
            scope.$.setState(codec.format(nextModel))
          } else {
            Callback.empty
          }
        }
      )
      .build
  }

  val doubleStringCodec: StringCodec[Double] = new StringCodec[Double] {
    def format(d: Double): String = d.toString
    def parse(s: String): Option[Double] = s.toDoubleOpt
  }

  val doubleView = AsStringView.component[Double]("DoubleView", doubleStringCodec)

  val booleanView = labelledCursorView[Boolean]("booleanView") { p =>
    MuiCheckbox(
      label = p.label,
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
