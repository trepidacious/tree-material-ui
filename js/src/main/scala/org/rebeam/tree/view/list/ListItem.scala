package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui.Mui
import japgolly.scalajs.react.ReactComponentC.ReqProps
import japgolly.scalajs.react.vdom.ReactTagOf
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactComponentU, ReactComponentU_, ReactElement, TopNode}
import org.rebeam.lenses.LensN
import org.rebeam.tree.ValueDelta.BooleanValueDelta
import org.rebeam.tree.{DLens, Delta}
import org.rebeam.tree.view.View._
import org.rebeam.tree.view.icon.Icons
import org.rebeam.tree.view.transition.CSSTransitionGroup
import org.rebeam.tree.view._
import org.scalajs.dom.html.Div

object ListItem {

  private val handleGrip = View.staticView("handle")(
    <.div(
      ^.className:= "tree-list-item__plain-handle",
      Icons.handleIcon
    )
  )

  private val handle = SortableHandle.wrap(handleGrip)(())

  case class ButtonAction(icon: ReactElement, cb: Callback)

  case class Props(avatar: ReactElement, content: ReactElement, iconButtons: List[ButtonAction], onClick: Callback, onClickAvatar: Callback, onClickContents: Callback, buttonBackgroundColor: Color)

  case class State(open: Boolean)

  class Backend(scope: BackendScope[Props, State]) {

    private val onClickEllipsis = scope.modState(s => s.copy(open = !s.open))
    private val close = scope.modState(s => s.copy(open = false))

    private def renderPlain(p: Props): ReactTagOf[Div] = {
      <.div(
        ^.key := "plain",
        ^.className := "tree-list-item__plain-outer",
        <.div(
          ^.onClick --> (p.onClick >> p.onClickAvatar),
          ^.className := "tree-list-item__plain-avatar",
          p.avatar
        ),
        <.div(
          ^.onClick --> (p.onClick >> p.onClickContents),
          ^.className := "tree-list-item__plain-contents",
          p.content
        ),
        <.div(
          ^.onClick --> onClickEllipsis,
          ^.className := "tree-list-item__plain-ellipsis",
          Icons.ellipsisIcon
        ),
        handle
      )
    }

    private def renderOpen(p: Props) = {
      val count = p.iconButtons.size

      <.div(
        ^.key := "open",
        ^.className := "tree-list-item__container",

        // Button panel
        <.div(
          ^.className := "tree-list-item__button-panel",
          ^.width := s"${count * 56}px",
          ^.backgroundColor := p.buttonBackgroundColor,
          p.iconButtons.map(
            b => <.div(
              ^.flex := "0 0 56px",
              ListItemIconButton(b.icon, b.cb >> close)
            )
          )
        ),

        // Left-hand sliding panel
        <.div(
          ^.className := s"tree-list-item__sliding-panel-left tree-list-item--open-$count",

          // Avatar
          <.div(
            ^.onClick --> (p.onClick >> p.onClickAvatar),
            ^.className := "tree-list-item__plain-avatar",
            p.avatar
          ),
          // Contents
          <.div(
            ^.onClick --> (p.onClick >> p.onClickContents),
            ^.className := "tree-list-item__plain-contents",
            ^.background := "#FFF",
            p.content
          ),
          // Padding
          <.div(
            ^.flex := "0 0 112px"

          )
        ),

        // Right-hand sliding panel
        <.div(
          ^.className := s"tree-list-item__sliding-panel-right",

          // Expand button
          <.div(
            ^.className := "tree-list-item__ellipsis_button",
            ^.onClick --> onClickEllipsis,
            <.div(
              ^.className := "tree-list-item__ellipsis",
              Icons.ellipsisIcon
            ),
            <.div(
              ^.className := "tree-list-item__cross",
              Icons.crossIcon
            )
          ),

          // Drag handle
          handle
        ),

        // Dividers
        <.div(^.className := "tree-list-item__divider-top tree-list-item__divider"),
        <.div(^.className := "tree-list-item__divider-bottom tree-list-item__divider")
      )
    }

    def render(p: Props, state: State): ReactComponentU_ = {
      CSSTransitionGroup(
        "tree-list-item--transition",
        enterTimeout = 250,
        leaveTimeout = 250,
        component = "div",
        className = "tree-list-item__transition-container"
      )(
        //NOTE we can pass children* here, so just need to get this
        //from list of divs in each case, to avoid double outer div.
        if (state.open) {
          renderOpen(p)
        } else {
          renderPlain(p)
        }
      )
    }
  }

  val component: ReqProps[Props, State, Backend, TopNode] = ReactComponentB[Props]("ListItem")
    .initialState(State(false))
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

  def apply(avatar: ReactElement, content: ReactElement, iconButtons: List[ButtonAction], onClick: Callback = Callback.empty, onClickAvatar: Callback = Callback.empty, onClickContents: Callback = Callback.empty, buttonBackgroundColor: Color = MaterialColor.Indigo(500)): ReactComponentU[Props, State, Backend, TopNode] =
    component(Props(avatar, content, iconButtons, onClick, onClickAvatar, onClickContents, buttonBackgroundColor))

  def twoLines(line1: String, line2: String): ReactTagOf[Div] =
    <.div(
      ^.className := "tree-list-item__two-lines-div",
      <.span(
        ^.className := "tree-list-item__two-lines-top",
        line1
      ),
      <.span(
        ^.className := "tree-list-item__two-lines-bottom",
        line2
      )
    )

  case class DeleteAction(delete: Action)

  def listItemWithDelete[U, A, D <: Delta[U, A]](name: String, firstLine: A => String, secondLine: A => String, avatar: A => ReactElement): ReqProps[Cursor[U, A, D, DeleteAction], Unit, Unit, TopNode] = cursorView[U, A, D, DeleteAction](name){
    cp => {
      val m = cp.model

      val buttons = List(
        ListItem.ButtonAction(
          Mui.SvgIcons.ActionDelete()(),
          cp.location.delete
        )
      )

      ListItem(avatar(m), ListItem.twoLines(firstLine(m), secondLine(m)), buttons)
    }
  }

  def listItemWithContentsAndDelete[U, A, D <: Delta[U, A]](name: String, contents: Cursor[U, A, D, DeleteAction] => ReactElement, avatar: A => ReactElement): ReqProps[Cursor[U, A, D, DeleteAction], Unit, Unit, TopNode] = cursorView[U, A, D, DeleteAction](name){
    cp => {
      val m = cp.model

      val buttons = List(
        //FIXME pass ActionCallback directly
        ListItem.ButtonAction(
          Mui.SvgIcons.ActionDelete()(),
          cp.location.delete.callback
        )
      )

      ListItem(avatar(m), contents(cp), buttons)
    }
  }

  case class EditAndDeleteActions(edit: Action, delete: Action)

  def listItemWithEditAndDelete[U, A, D <: Delta[U, A]](name: String, firstLine: A => String, secondLine: A => String, avatar: A => ReactElement): ReqProps[Cursor[U, A, D, EditAndDeleteActions], Unit, Unit, TopNode] = cursorView[U, A, D, EditAndDeleteActions](name){
    cp => {
      val m = cp.model

      val buttons = List(
        ListItem.ButtonAction(
          Mui.SvgIcons.ActionDelete()(),
          cp.location.delete
        ),
        ListItem.ButtonAction(
          Mui.SvgIcons.ContentCreate()(),
          cp.location.edit
        )
      )

      ListItem(avatar(m), ListItem.twoLines(firstLine(m), secondLine(m)), buttons, onClick = cp.location.edit)
    }
  }

  def listItemWithCompleteEditAndDelete[U, A, D <: Delta[U, A]](
    name: String, firstLine: A => String, secondLine: A => String, completionLens: DLens[U, A, D, Boolean, BooleanValueDelta[U]]
  ): ReqProps[Cursor[U, A, D, EditAndDeleteActions], Unit, Unit, TopNode] = cursorView(name){
    cp => {
      val m = cp.model

      val avatar = <.div(
        ^.position := "relative",
        ^.top := "8px",
        ^.left := "8px",
        booleanViewUnlabelled(cp.zoom(completionLens).label(""))
      )

      val buttons = List(
        ListItem.ButtonAction(
          Mui.SvgIcons.ActionDelete()(),
          cp.location.delete
        ),
        ListItem.ButtonAction(
          Mui.SvgIcons.ContentCreate()(),
          cp.location.edit
        )
      )

      ListItem(avatar, ListItem.twoLines(firstLine(m), secondLine(m)), buttons, onClickContents = cp.location.edit)
    }
  }


}
