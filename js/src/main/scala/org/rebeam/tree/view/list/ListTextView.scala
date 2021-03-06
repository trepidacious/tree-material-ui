package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.view._
import org.scalajs.dom.html.Div

object ListTextView {

  private val focus = (input: MuiTextFieldM) =>
    if (input != null) {
      input.focus
    }

  case class State(editing: Boolean)

  class Backend(scope: BackendScope[Cursor[String, String], State]) {

    private val edit = scope.modState(s => s.copy(editing = true))
    private val unedit = scope.modState(s => s.copy(editing = false))

    private def renderPlain(c: Cursor[String, String]): VdomTagOf[Div] = {
      <.div(
        ^.key := "plain",
        ^.className := "tree-list-text-view__plain",
        ^.onDblClick --> edit,
        ^.onClick --> edit,
        ^.onFocus --> edit,
        ^.onBlur --> unedit,
        ^.tabIndex := 0,
        c.model
      )
    }

    private def renderEditing(c: Cursor[String, String]) = {
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
          onFocus =  (e: ReactFocusEventFromInput) => Callback{e.target.select},
          // Normal bound editing
          onChange = (e: ReactEventFromInput, s: String) => e.preventDefaultCB >> c.set(s),
          hintText = c.location: VdomNode//,
//          onEnterKeyDown = (e: ReactEventFromInput) => e.preventDefaultCB >> Callback{println("ENTER!")},
//          onKeyDown = (e: ReactKeyboardEvent) => if (e.keyCode == 9) {
//            e.preventDefaultCB >> Callback{println("TAB!")}
//          } else {
//            Callback.empty
//          }
        )()
      )
    }

    def render(p: Cursor[String, String], state: State): VdomTagOf[Div] = {
      if (state.editing) {
        renderEditing(p)
      } else {
        renderPlain(p)
      }
    }
  }

  val component =
    ScalaComponent.builder[Cursor[String, String]]("ListTextView")
    .initialState(State(false))
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

  def apply(c: Cursor[String, String]) =
    component(c)

}
