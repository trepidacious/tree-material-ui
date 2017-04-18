package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui._
import io.circe.Encoder
import japgolly.scalajs.react.ReactComponentC.ReqProps
import japgolly.scalajs.react.{Callback, ReactComponentU_, TopNode}
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.view._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view.infinite.Infinite
import org.rebeam.tree.view.measure.CursorPHeightView
import org.rebeam.tree.view.pages.Pages
import org.rebeam.tree.view.transition._

import scala.scalajs.js

object ListView {

  /**
    * Create a component viewing a list
    *
    * @param name      The name of the component
    * @param toItem    Function from an item to a finder, used for CursorP.zoomAllMatchesP
    * @param itemToKey Maps items to their react keys
    * @param itemView  Component viewing items
    * @param subheader The subheader text to display in list
    * @param fEncoder  Encoder for finder
    * @tparam A The type of list element
    * @tparam C The type of current page
    * @tparam P The type of all pages
    * @tparam F The type of finder used to find items
    * @return A view of the list, with infinite scrolling, suitable for use in a SortableContainer
    */
  def legacy[A, C, P, F <: A => Boolean](
                                         name: String,
                                         toItem: A => F,
                                         itemToKey: A => js.Any,
                                         itemView: ReqProps[CursorP[A, Pages[C, P]], Unit, Unit, TopNode],
                                         subheader: String,
                                         mode: ListMode = ListMode.Infinite)(implicit fEncoder: Encoder[F]): ((IndexChange) => Callback) => (CursorP[List[A], Pages[C, P]]) => ReactComponentU_ = {
    ListView[List[A], Pages[C, P], CursorP[A, Pages[C, P]]](
      name,
      _.zoomAllMatchesP(toItem),
      c => itemToKey(c.model),
      itemView,
      subheader,
      mode
    )
  }

  def withAction[R, P, A, Q](
    name: String,
    listCursorToItems: CursorP[R, P] => List[CursorP[A, Q]],
    itemToKey: A => js.Any,
    itemView: ReqProps[CursorP[A, Q], Unit, Unit, TopNode],
    subheader: String,
    mode: ListMode = ListMode.Infinite
   ): ((IndexChange) => Callback) => (CursorP[R, P]) => ReactComponentU_ = {
    ListView[R, P, CursorP[A, Q]](
      name,
      listCursorToItems,
      c => itemToKey(c.model),
      itemView,
      subheader,
      mode
    )
  }

  def usingMatches[R, P, A, Q, F <: A => Boolean](
    name: String,
    rootToItems: CursorP[R, P] => Cursor[List[A]],
    itemToFinder: A => F,
    itemAndCursorToAction: (A, CursorP[R, P]) => Q,
    itemToKey: A => js.Any,
    itemView: ReqProps[CursorP[A, Q], Unit, Unit, TopNode],
    subheader: String,
    mode: ListMode = ListMode.Infinite
  )(implicit fEncoder: Encoder[F]): ((IndexChange) => Callback) => (CursorP[R, P]) => ReactComponentU_ = {
    ListView.withAction[R, P, A, Q](
      name,
      (cp: CursorP[R, P]) => rootToItems(cp).zoomAllMatches(itemToFinder).map(ca => ca.withP(itemAndCursorToAction(ca.model, cp))),
      c => itemToKey(c),
      itemView,
      subheader,
      mode
    )
  }

  def usingId[R, P, A <: HasId[A], Q](
    name: String,
    rootToItems: CursorP[R, P] => Cursor[List[A]],
    itemAndCursorToAction: (A, CursorP[R, P]) => Q,
    itemView: ReqProps[CursorP[A, Q], Unit, Unit, TopNode],
    subheader: String,
    mode: ListMode = ListMode.Infinite
  )(implicit fEncoder: Encoder[FindById[A]]): ((IndexChange) => Callback) => (CursorP[R, P]) => ReactComponentU_ = {
    ListView.usingMatches[R, P, A, Q, FindById[A]](
      name,
      rootToItems,
      a => FindById[A](a.id),
      itemAndCursorToAction,
      a => a.id.toString(),
      itemView,
      subheader,
      mode
    )
  }

  sealed trait ListMode
  object ListMode {
    case object Infinite extends ListMode
    case object Finite extends ListMode
  }

  /**
    * Create a component viewing a list
    * @param name         The name of the component
    * @param listCursorToItems  Produce a list of elements from the model
    * @param itemToKey    Produce a key for each element
    * @param itemView     A view for an element
    * @param subheader    The subheader text to display in list
    * @tparam L           The type of the list-like model
    * @tparam P           The type P for CursorP
    * @tparam A           The type of list element
    * @return             A view of the list, with infinite scrolling, suitable for use in a SortableContainer
    */
  def apply[L, P, A](
                                      name: String,
                                      listCursorToItems: CursorP[L, P] => List[A],
                                      itemToKey: A => js.Any,
                                      itemView: ReqProps[A, Unit, Unit, TopNode],
                                      subheader: String,
                                      mode: ListMode = ListMode.Infinite): ((IndexChange) => Callback) => (CursorP[L, P]) => ReactComponentU_ =
  {
    val sortableElement = SortableElement.wrap(itemView)

    mode match {
      // Wrap in an Infinite for performance on long lists
      case ListMode.Infinite =>
        // Use a height view to get us the height of the rendered element as an extra part of prop.
        // This lets us scale the Infinite list appropriately to fill space.
        val view = CursorPHeightView[L, P](name) {
          (cp, height) =>
            val h: Int = height.map(_.toInt).getOrElse(60)

            // We need to apply a style by class to get the Infinite to be 100% height rather than the
            // "height: containerHeight" inline style it sets on itself. This allows it to resize to fill
            // available space, then be measured by Measure, which adjusts the containerHeight. This is
            // neater than wrapping in a "height: 100%" div, and also works with react-sortable-hoc, which
            // expects the top level component to be the one containing the sortable elements. Using a div
            // breaks this and so breaks the nice feature where dragging to container edge starts scrolling.
            Infinite(elementHeight = 60, containerHeight = h, className = "tree-infinite--height-100-percent")(
              MuiSubheader(inset = true, style = js.Dynamic.literal("height" -> "60px", "padding-top" -> "8px"))(subheader)
                :: listCursorToItems(cp).zipWithIndex.map {
                case (a, index) => sortableElement(SortableElement.Props(key = itemToKey(a), index = index))(a)
              }
            )
        }
        val sortableView = SortableContainer.wrap(view)
        (onIndexChange: IndexChange => Callback) => sortableView(p(onIndexChange))

      // Don't wrap with infinite, therefore doesn't need a height view.
      // For this case we can also provide enter/leave transitions.
      case ListMode.Finite =>
        val view = cursorPView[L, P](name) {
          cp =>
            CSSTransitionGroup(
              "tree-list-view--transition",
              enterTimeout = 250, // Animation should take 225ms
              leaveTimeout = 220, // Animation should take 195ms
              component = "div",
              className = "tree-list-view__transition-container"
            )(
              MuiSubheader(inset = true, style = js.Dynamic.literal("height" -> "60px", "padding-top" -> "8px"))(subheader)
                :: listCursorToItems(cp).zipWithIndex.map {
                case (a, index) => sortableElement(SortableElement.Props(key = itemToKey(a), index = index))(a)
              }
            )
        }
        val sortableView = SortableContainer.wrap(view)
        (onIndexChange: IndexChange => Callback) => sortableView(p(onIndexChange))
    }
  }

  def p(onSortEnd: IndexChange => Callback = p => Callback{}): SortableContainer.Props =
    SortableContainer.Props(
      onSortEnd = onSortEnd,
      useDragHandle = true,
      helperClass = "react-sortable-handler"
    )

}
