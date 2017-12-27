package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react.ReactComponentC.ReqProps
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.ReactTagOf
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.ValueDelta._
import org.rebeam.tree.view._
import org.scalajs.dom.html.Div

object ListTextView {

  type StringCursor = Cursor[Nothing, String, StringValueDelta, String]

  private val focus = (input: MuiTextFieldM) =>
    if (input != null) {
      input.focus
    }

  case class State(editing: Boolean)

  class Backend(scope: BackendScope[StringCursor, State]) {

    private val edit = scope.modState(s => s.copy(editing = true))
    private val unedit = scope.modState(s => s.copy(editing = false))

    private def renderPlain(c: StringCursor): ReactTagOf[Div] = {
      <.div(
        ^.key := "plain",
        ^.className := "tree-list-text-view__plain",
        ^.onDblClick --> edit,
        ^.onClick --> edit,
        ^.onFocus --> edit,
        ^.onBlur --> unedit,
        ^.tabIndex := "0",
        c.model
      )
    }

    private def renderEditing(c: StringCursor) = {
      <.div(
        ^.key := "editing",
        ^.className := "tree-list-text-view__editing",
        ^.onBlur --> unedit,
        MuiTextField(
          value = c.model,
          key = "tv",
          // On creation, request focus (also triggering selection via onFocus)
          ref = focus,
          fullWidth = true,
          // On focus, select all text for spreadsheet cell-style editing
          onFocus =  (e: ReactFocusEventI) => Callback{e.target.select},
          // Normal bound editing
          onChange = (e: ReactEventI) => e.preventDefaultCB >> c.act(StringValueDelta(e.target.value)),
          hintText = c.location: ReactNode//,
//          onEnterKeyDown = (e: ReactEventI) => e.preventDefaultCB >> Callback{println("ENTER!")},
//          onKeyDown = (e: ReactKeyboardEvent) => if (e.keyCode == 9) {
//            e.preventDefaultCB >> Callback{println("TAB!")}
//          } else {
//            Callback.empty
//          }
        )()
      )
    }

    def render(p: StringCursor, state: State): ReactTagOf[Div] = {
      if (state.editing) {
        renderEditing(p)
      } else {
        renderPlain(p)
      }
    }
  }

  val component: ReqProps[StringCursor, State, Backend, TopNode] =
    ReactComponentB[StringCursor]("ListTextView")
    .initialState(State(false))
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

  def apply(c: StringCursor): ReactComponentU[StringCursor, State, Backend, TopNode] =
    component(c)

}
