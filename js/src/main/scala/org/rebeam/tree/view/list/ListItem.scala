package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui._
import MuiSvgIcon.SvgIconApply
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react._
import monocle.Lens
import org.rebeam.tree.view.View._
import org.rebeam.tree.view.icon.Icons
import org.rebeam.tree.view.transition.CSSTransitionGroup
import org.rebeam.tree.view._
import org.scalajs.dom.html.Div

object ListItem {

  val HandleGrip =
    ScalaComponent.builder[String]("HandleGrip")
      .render_P(s =>
        <.div(
          ^.className:= "tree-list-item__plain-handle",
          Icons.handleIcon
        )
      ).build

  private val handle = SortableHandle.wrap(HandleGrip)("")

//  val HandleGrip =
//    ScalaComponent.static("HandleGrip")(
//      <.div(
//        ^.className:= "tree-list-item__plain-handle",
//        Icons.handleIcon
//      )
//    )
//  private val handle = HandleGrip()

  //FIXME make this accept an action?
  case class ButtonAction(icon: VdomElement, action: Action)

  case class Props(avatar: VdomElement, content: VdomElement, iconButtons: List[ButtonAction], onClick: Callback, onClickAvatar: Callback, onClickContents: Callback, buttonBackgroundColor: Color, draggable: Boolean)

  case class State(open: Boolean)

  class Backend(scope: BackendScope[Props, State]) {

    private val onClickEllipsis: Callback = scope.modState(s => s.copy(open = !s.open))
    private val close = scope.modState(s => s.copy(open = false))

    private def renderPlain(p: Props): VdomTagOf[Div] = {
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
          ^.onClick --> (if (p.iconButtons.isEmpty) p.onClick else onClickEllipsis),
          ^.className := "tree-list-item__plain-ellipsis",
          if (p.iconButtons.isEmpty) Icons.navigateRightIcon else Icons.ellipsisIcon
        ),
        handle.when(p.draggable)
      )
    }

    private def renderOpen(p: Props): VdomTagOf[Div] = {
      val count = p.iconButtons.size

      <.div(
        ^.key := "open",
        ^.className := "tree-list-item__container",

        // Button panel
        <.div(
          ^.className := "tree-list-item__button-panel",
          ^.width := s"${count * 56}px",
          ^.backgroundColor := p.buttonBackgroundColor.toString,
          p.iconButtons.toTagMod(
            b => <.div(
              ^.flex := "0 0 56px",
              ListItemIconButton(b.icon, b.action.callback >> close)
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

    def render(p: Props, state: State): JsComponent.Unmounted[CSSTransitionGroup.Props, Null] = {
      CSSTransitionGroup(
        "tree-list-item--transition",
        enterTimeout = 250,
        leaveTimeout = 250,
        component = "div",
        className = "tree-list-item__transition-container"
      )(
        //NOTE we can pass children* here, so just need to get this
        //from list of divs in each case, to avoid double outer div.
        if (state.open && p.iconButtons.nonEmpty) {
          renderOpen(p)
        } else {
          renderPlain(p)
        }
      )
    }
  }

  val component = ScalaComponent.builder[Props]("ListItem")
    .initialState(State(false))
    .backend(new Backend(_))
    .render(s =>
      s.backend.render(s.props, s.state)
    )
    .build

  def apply(
    avatar: VdomElement,
    content: VdomElement,
    iconButtons: List[ButtonAction],
    onClick: Callback = Callback.empty,
    onClickAvatar: Callback = Callback.empty,
    onClickContents: Callback = Callback.empty,
    buttonBackgroundColor: Color = MaterialColor.Indigo(500),
    draggable: Boolean = true
  ) = component(Props(avatar, content, iconButtons, onClick, onClickAvatar, onClickContents, buttonBackgroundColor, draggable))

  def twoLines(line1: String, line2: String): VdomTagOf[Div] =
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

  def listItemWithDelete[A](name: String, firstLine: A => String, secondLine: A => String, avatar: A => VdomElement) = cursorView[A, DeleteAction](name){
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

  def listItemWithContentsAndDelete[A](name: String, contents: Cursor[A, DeleteAction] => VdomElement, avatar: A => VdomElement) = cursorView[A, DeleteAction](name){
    cp => {
      val m = cp.model

      val buttons = List(
        //FIXME pass ActionCallback directly
        ListItem.ButtonAction(
          Mui.SvgIcons.ActionDelete()(),
          cp.location.delete
        )
      )

      ListItem(avatar(m), contents(cp), buttons)
    }
  }

  case class EditAndDeleteActions(edit: Action, delete: Action)

  def listItemWithEditAndDelete[A](
    name: String,
    firstLine: A => String,
    secondLine: A => String,
    avatar: A => VdomElement
  ) = cursorView[A, EditAndDeleteActions](name){
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

  def listItemWithCompleteEditAndDelete[A](
    name: String,
    firstLine: A => String,
    secondLine: A => String,
    completionLens: Lens[A, Boolean]
  ) = cursorView[A, EditAndDeleteActions](name) {
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

  def listItemWithNavigate[A](name: String, firstLine: A => String, secondLine: A => String, avatar: A => VdomElement, action: A => Action) = ScalaComponent.builder[A](name)
    .render_P(
      a => ListItem(
        avatar = avatar(a),
        content = ListItem.twoLines(firstLine(a), secondLine(a)),
        iconButtons = Nil,
        onClick = action(a),
        draggable = false)
  ).build

}
