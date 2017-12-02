package org.rebeam.tree.view

import cats.syntax.either._
import chandu0101.scalajs.react.components.Implicits._
import chandu0101.scalajs.react.components.materialui._
import io.circe.Encoder
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.{ReactStyle, ReactTagOf}

import scala.scalajs.js.UndefOr
import Mui.Styles.colors
import japgolly.scalajs.react.ReactComponentC.ReqProps

import scala.language.implicitConversions
import scala.scalajs.js
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.icon.Icons
import org.scalajs.dom.html.Span

object View {

  def touch(c: Callback): js.UndefOr[ReactTouchEventH => Callback] = {
    e: ReactTouchEventH => e.preventDefaultCB >> c
  }

  def view[A](name: String, overlay: Boolean = true)(render: A => ReactElement) =
    ReactComponentB[A](name).render_P(render).build

  def cursorView[A, P](name: String)(render: Cursor[A, P] => ReactElement): ReqProps[Cursor[A, P], Unit, Unit, TopNode] =
    ReactComponentB[Cursor[A, P]](name).render_P(render).configure(Reusability.shouldComponentUpdate).build

  def staticView(name: String)(e: ReactElement) = ReactComponentB[Unit](name)
    .render(_ => e)
    .build

  val spinner = staticView("Spinner")(
    MuiCircularProgress(mode = DeterminateIndeterminate.indeterminate)()
  )

  val textView = cursorView[String, String]("textView") { p =>
    MuiTextField(
      value = p.model,
      onChange = (e: ReactEventI) => e.preventDefaultCB >>  p.set(e.target.value),
      floatingLabelText = p.location: ReactNode
    )()
  }

  val textViewHero = cursorView[String, String]("textViewHero") { p =>
    MuiTextField(
      value = p.model,
      onChange = (e: ReactEventI) => e.preventDefaultCB >>  p.set(e.target.value),
//      floatingLabelText = p.label: ReactNode,
//      floatingLabelStyle = js.Dynamic.literal("font-size" -> "16px", "color" -> "rgba(255, 255, 255, 0.87"),
      hintText = p.location: ReactNode,
      hintStyle = js.Dynamic.literal("color" -> "rgba(255, 255, 255, 0.87)"),
      style = js.Dynamic.literal("font-size" -> "24px"),
      inputStyle = js.Dynamic.literal("color" -> "rgba(255, 255, 255, 1.00)"),
      underlineStyle = js.Dynamic.literal(
        "bottom" -> "6px"
      ),
      underlineDisabledStyle = js.Dynamic.literal(
        "bottom" -> "6px"
      ),
      underlineFocusStyle = js.Dynamic.literal(
        "border-bottom" -> "2px solid rgb(224, 224, 224)",
        "bottom" -> "6px"
      )
    )()
  }

  //FIXME work out a better way to align with textViewHero
  def labelHero(s: String): ReactTagOf[Span] = <.span(
    ^.fontSize := "23px",
    ^.paddingTop := "16px",
    ^.color := "rgba(255, 255, 255, 1.00)",
    s
  )

  val textViewPlainLabel = cursorView[String, String]("textViewPlainLabel") { p =>
    MuiTextField(
      value = p.model,
      onChange = (e: ReactEventI) => e.preventDefaultCB >>  p.set(e.target.value),
      hintText = p.location: ReactNode
    )()
  }

  trait StringCodec[A] {
    def format(a: A): String
    def parse(s: String): Either[String, A]
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

    class Backend[A](scope: BackendScope[Cursor[A, String], (String, Boolean)])(implicit codec: StringCodec[A], encoder: Encoder[A]) {

      def render(props: Cursor[A, String], state: (String, Boolean)) = {
        val model = props.model

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
          case Left(e) => e
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
              case Right(newModel) if newModel != model => e.preventDefaultCB >> scope.setState((input, false)) >> props.set(newModel)

              // Otherwise just change state - we are editing without producing a valid new value,
              // but we may be on the way to a valid new value
              case _ => e.preventDefaultCB >> scope.setState((input, false))
            }
          },

          //On blur, update state to match model if it does not parse to model
          onBlur = (e: ReactEventI) => {
            val parsed = codec.parse(state._1)
            parsed match {
              case Right(p) if p == model => Callback.empty
              case _ => scope.setState((codec.format(model), false))
            }
          },
//          errorText = error,
//          hintText = props.label: ReactNode
            floatingLabelText = props.location: ReactNode
        )()
      }
    }

    def component[A](name: String, codec: StringCodec[A])(implicit encoder: Encoder[A]) = ReactComponentB[Cursor[A, String]](name)
      .getInitialState[(String, Boolean)](scope => (scope.props.model.toString, false))
      .backend(new Backend[A](_)(codec, encoder))
      .render(s => s.backend.render(s.props, s.state))
      .componentWillReceiveProps(
        scope => if (scope.currentProps.model != scope.nextProps.model) {
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
    override def parse(s: String): Either[String, Double] = try {
      Right(s.toDouble)
    } catch {
      case e: NumberFormatException => Left("Valid number required (e.g. 1, -1.1, 1.1E1)")
    }
    //Remove anything but + - . e E or a digit
    override def prefilter(s: String): String = s.replaceAll("""[^\+\-\.eE\d]""", "")
  }

  val doubleView = AsStringView.component[Double]("doubleView", doubleStringCodec)

  val intStringCodec: StringCodec[Int] = new StringCodec[Int] {
    def format(d: Int): String = d.toString
    def parse(s: String): Either[String, Int] = try {
      Right(s.toInt)
    } catch {
      case e: NumberFormatException => Left("Valid whole number required (e.g. 1, 100)")
    }
    //Remove anything but + - or a digit
    override def prefilter(s: String): String = s.replaceAll("""[^\+\-\d]""", "")
  }

  val intView = AsStringView.component[Int]("intView", intStringCodec)

  val booleanView = cursorView[Boolean, String]("booleanView") { p =>
    MuiCheckbox(
      label = p.location,
      checked = p.model,
      onCheck = (e: ReactEventH, b: Boolean) => e.preventDefaultCB >> p.set(!p.model)
    )()
  }


  val booleanViewUnlabelled = cursorView[Boolean, String]("booleanView") { p =>
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

  implicit def color2MuiColor(c: Color): MuiColor = c.toString().asInstanceOf[MuiColor]
  implicit def color2UndefOrMuiColor(c: Color): UndefOr[MuiColor] = c.toString().asInstanceOf[MuiColor]
  implicit final val _react_styleColor  : ReactStyle.ValueType[Color] = ReactStyle.ValueType.stringValue

  val avatarText = ReactComponentB[(String, Color)]("avatarText")
    .render(d => {
      val text = d.props._1
      val color = d.props._2
      //Adapt font size to number of digits
      val fontSize = text.length match {
        case 1 => 20
        case 2 => 20
        case 3 => 16
        case 4 => 13
        case 5 => 11
        case 6 => 10
        case _ => 8
      }
      MuiAvatar(
        style = js.Dynamic.literal("font-size" -> s"${fontSize}px"),
        color = colors.white,
        backgroundColor = color
      )(text: ReactNode)
    }).build

  val avatarArcHash: ReqProps[Int, Unit, Unit, TopNode] = ReactComponentB[Int]("avatarArcHash")
    .render(d => {
      val hash = d.props

      val color = MaterialColor.backgroundForIndex((hash >> 24)&0xFF)
      MuiAvatar(
        color = colors.white,
        backgroundColor = color

      )(Icons.makeArcHashIcon(hash))
    }).build

  def coloredCardButton(label: String, primary: Boolean = false, secondary: Boolean = false)(callback: Callback) = {
    MuiFlatButton(
      style = js.Dynamic.literal("color" -> MaterialColor.White()),
      hoverColor = MaterialColor.White().copy(a = 50),
      rippleColor = MaterialColor.White(),
      label = label,
      primary = primary,
      secondary = secondary,
      onTouchTap = touch(callback)
    )()
  }

  def coloredCard(color: Color, title: ReactNode, content: ReactNode, actions: List[(String, Callback)]) = {
    MuiCard(
      style = js.Dynamic.literal(
        "background-color" -> color,
        "margin" -> "8px"
      )
    )(
      <.div(
        ^.className := "tree-colored-card__title",
        title
      ),

      <.div(
        ^.className := "tree-colored-card__content",
        content
      ),

      <.div(
        ^.className := "tree-colored-card__buttons",
        actions.zipWithIndex.map{
          case((text, action), index) => coloredCardButton("Details", primary = index==0)(action)
        }
      )
    )
  }

}
