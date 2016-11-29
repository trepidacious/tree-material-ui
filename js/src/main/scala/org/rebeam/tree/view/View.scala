package org.rebeam.tree.view

import cats.data.Xor
import chandu0101.scalajs.react.components.Implicits._
import chandu0101.scalajs.react.components.materialui._
import io.circe.Encoder
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability

import scala.scalajs.js
import scala.scalajs.js.UndefOr

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

  trait StringCodec[A] {
    def format(a: A): String
    def parse(s: String): Xor[String, A]
    def prefilter(s: String): String
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

    class Backend[A](scope: BackendScope[LabelledCursor[A], (String, Boolean)])(implicit codec: StringCodec[A], encoder: Encoder[A]) {

      def render(props: LabelledCursor[A], state: (String, Boolean)) = {
        val model = props.cursor.model

        // If we have had a prop change since the last time we set state,
        // and state does not now represent model, move to model
        val text = if (state._2 && !codec.parse(state._1).toOption.contains(model)) {
          codec.format(model)

        //If we have not had a prop change since we last set state, or state
        // matches model anyway, just use state
        } else {
          state._1
        }

        val error: UndefOr[ReactNode] = codec.parse(text) match {
          case Xor.Left(e) => e
          case _ => js.undefined
        }

        MuiTextField(
          value = text,
          onChange = (e: ReactEventI) => {
            val input = codec.prefilter(e.target.value)
            val parsed = codec.parse(input)
            parsed match {
              // If we have a parsed new model, and it is different to cursor's model, then set new state and model
              // We set the state so that if the input is a non-standard representation of the model, it will still be
              // preserved, since it will be in place on our next render when we check it against the new prop.
              case Xor.Right(newModel) if newModel != model => e.preventDefaultCB >> scope.setState((input, false)) >> props.cursor.set(newModel)

              // Otherwise just change state - we are editing without producing a valid new value,
              // but we may be on the way to a valid new value
              case _ => e.preventDefaultCB >> scope.setState((input, false))
            }
          },

          //On blur, update state to match model if it does not parse to model
          onBlur = (e: ReactEventI) => {
            val parsed = codec.parse(state._1)
            parsed match {
              case Xor.Right(p) if p == model => Callback.empty
              case _ => scope.setState((codec.format(model), false))
            }
          },
          errorText = error,
          floatingLabelText = props.label: ReactNode
        )()
      }
    }

    def component[A](name: String, codec: StringCodec[A])(implicit encoder: Encoder[A]) = ReactComponentB[LabelledCursor[A]](name)
      .getInitialState[(String, Boolean)](scope => (scope.props.cursor.model.toString, false))
      .backend(new Backend[A](_)(codec, encoder))
      .render(s => s.backend.render(s.props, s.state))
      .componentWillReceiveProps(
        scope => if (scope.currentProps.cursor.model != scope.nextProps.cursor.model) {
          scope.$.modState(s => (s._1, true))
        } else {
          Callback.empty
        }
      )
      .configure(Reusability.shouldComponentUpdate)
      .build
  }

  val doubleStringCodec: StringCodec[Double] = new StringCodec[Double] {
    override def format(d: Double): String = d.toString
    override def parse(s: String): Xor[String, Double] = try {
      Xor.Right(s.toDouble)
    } catch {
      case e: NumberFormatException => Xor.Left("Valid number required (e.g. 1, -1.1, 1.1E1)")
    }
    //Remove anything but + - . e E or a digit
    override def prefilter(s: String): String = s.replaceAll("""[^\+\-\.eE\d]""", "")
  }

  val doubleView = AsStringView.component[Double]("doubleView", doubleStringCodec)

  val intStringCodec: StringCodec[Int] = new StringCodec[Int] {
    def format(d: Int): String = d.toString
    def parse(s: String): Xor[String, Int] = try {
      Xor.Right(s.toInt)
    } catch {
      case e: NumberFormatException => Xor.Left("Valid whole number required (e.g. 1, 100)")
    }
    //Remove anything but + - or a digit
    override def prefilter(s: String): String = s.replaceAll("""[^\+\-\d]""", "")
  }

  val intView = AsStringView.component[Int]("intView", intStringCodec)

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
