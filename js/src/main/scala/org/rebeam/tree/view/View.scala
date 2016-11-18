package org.rebeam.tree.view

import org.rebeam.tree.TreeUtils._

import chandu0101.scalajs.react.components.Implicits._
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

  /**
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
//  val doubleView = labelledCursorView[Double]("doubleView") { p =>
//    MuiTextField(
//      `type` = "number",
//      value = p.cursor.model.toString,
//      onChange = (e: ReactEventI) => e.preventDefaultCB >> p.cursor.set(e.target.value.toDouble),
//      floatingLabelText = p.label: ReactNode
//    )()
//  }

  val doubleView = DoubleView.component

  object DoubleView {

    def format(d: Double): String = d.toString
    //TODO make into an Xor with error to display as validation
    def parse(s: String): Option[Double] = s.toDoubleOpt

    class Backend(scope: BackendScope[LabelledCursor[Double], Option[String]]) {

      def render(props: LabelledCursor[Double], state: Option[String]) = {

        val formattedProps = format(props.cursor.model)

        println("Rendering...")

        val text = state match {
          // No user input yet - use props
          case None => {
            println("No user input, using formattedProps " + formattedProps)
            formattedProps
          }

          // User input
          case Some(input) => {
            println("We have user input " + input)
            val parsedInput = parse(input)
            parsedInput match {

              //If input can't be parsed we need to leave it alone
              case None => {
                println("Unparseable input so using directly")
                input
              }

              // If input can be parsed and matches props, then use it
              case Some(d) if d == props.cursor.model => {
                println("Parsed input to " + d + ", matches model " + props.cursor.model)
                input
              }

              case Some(d) => {
                println("Parsed input to " + d + ", mismatches model " + props.cursor.model + " so using formattedProps " + formattedProps)
                formattedProps
              }

//              // Otherwise use the props
//              case None => {
//                println("Couldn't parse input so using formattedProps " + formattedProps)
//                formattedProps
//              }
            }

          }
        }

        MuiTextField(
//          `type` = "number",
          value = text,
          onChange = (e: ReactEventI) => {
            val input = e.target.value
            val parsed = parse(input)
            println("onChange with input " + input + " parsed to " + parsed)
            parsed match {
              // If user is entering invalid text, we need to let them continue,
              // hopefully they are moving towards something parseable (e.g. for
              // a decimal, entering a new decimal point before removing the old
              // one). When they finish it will overwrite whatever props we have.
              case None => {
                println("Couldn't parse input so just setting state to Some(" + input + ") ... ")
                e.preventDefaultCB >> scope.setState(Some(input)) >> Callback{"... set state to Some(" + input + ")"}
              }

              // If text is valid, update the state then prop
              case Some(d) => {
                println("Parsed input so setting state to Some(" + input + ") then props to " + d)
                e.preventDefaultCB >> scope.setState(Some(input)) >> props.cursor.set(d) >> Callback{"... set state to Some(" + input + ") and props to " + d}
              }
            }

          },
          floatingLabelText = props.label: ReactNode
        )()
      }

    }

    val component = ReactComponentB[LabelledCursor[Double]]("DoubleView")
//      .getInitialState(scope => scope.props.cursor.model.toString)
      .initialState(None: Option[String])
      .backend(new Backend(_))
      .render(s => s.backend.render(s.props, s.state))
      .componentWillReceiveProps(scope => {
        println("Will receive props " + scope.nextProps.cursor.model + " so setting state to that")
        scope.$.setState(Some(scope.nextProps.cursor.model.toString))
      })
      .build
  }



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
