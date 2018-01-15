package org.rebeam.tree.view.list

import chandu0101.scalajs.react.components.materialui._
import io.circe.Encoder
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomNode
import org.rebeam.tree.Searchable
import org.rebeam.tree.ref._
import org.rebeam.tree.sync._
import org.rebeam.tree.view._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view.infinite.Infinite
import org.rebeam.tree.view.measure.CursorHeightView
import org.rebeam.tree.view.pages.Pages
import org.rebeam.tree.view.transition._
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js

//object ListView {
//
//  /**
//    * Create a component viewing a list
//    *
//    * @param name      The name of the component
//    * @param toItem    Function from an item to a finder, used for CursorP.zoomAllMatchesP
//    * @param itemToKey Maps items to their react keys
//    * @param itemView  Component viewing items
//    * @param subheader The subheader text to display in list
//    * @param fEncoder  Encoder for finder
//    * @tparam A The type of list element
//    * @tparam C The type of current page
//    * @tparam P The type of all pages
//    * @tparam F The type of finder used to find items
//    * @return A view of the list, with infinite scrolling, suitable for use in a SortableContainer
//    */
//  def legacy[A, C, P, F <: A => Boolean](
//    name: String,
//    toItem: A => F,
//    itemToKey: A => js.Any,
//    itemView: Cursor[A, Pages[C, P]] => VdomElement,
//    subheader: String,
//    mode: ListMode = ListMode.Infinite
//  )(implicit
//    fEncoder: Encoder[F],
//    s: Searchable[A, Guid]
//  ): (IndexChange => Callback) => (Cursor[List[A], Pages[C, P]]) => JsComponent.Unmounted[js.Object, Null] = {
//    ListView[List[A], Pages[C, P], Cursor[A, Pages[C, P]]](
//      name,
//      _.zoomAllMatches(toItem),
//      c => itemToKey(c.model),
//      itemView,
//      subheader,
//      mode
//    )
//  }
//
//  /**
//    * Create a list view
//    * @param name               Name of view
//    * @param listCursorToItems  Take a cursor with root model (type R) and location as a page (type P) and yield a list
//    *                           of cursors to the list items (type A) and locations as actions on them (type Q). Note we
//    *                           are using the location parameter as an action to perform on list items.
//    * @param itemToKey          Get a key for given list item
//    * @param itemView           Get a view for Cursor[A, Q], which displays list items of type A, allowing actions to
//    *                           be performed using location of type Q.
//    * @param subheader          Subheader text for the list
//    * @param mode               Mode for list display
//    * @tparam R                 The type of root model - some data type from which we can get to a list of items - doesn't
//    *                           need to actually be a list
//    * @tparam P                 The type of location for the root cursor - normally some kind of "page" location
//    * @tparam A                 The type of list items
//    * @tparam Q                 The type of location for list items - often some kind of action that can be performed
//    *                           on list items, but can be an actual location.
//    * @return                   A List view
//    */
//  def withAction[R, P, A, Q, CT[-p, +u] <: CtorType[p, u], U](
//    name: String,
//    listCursorToItems: Cursor[R, P] => List[Cursor[A, Q]],
//    itemToKey: A => js.Any,
//    itemView: GenericComponent[Cursor[A, Q], CT, U],
//    subheader: String,
//    mode: ListMode = ListMode.Infinite
//   ): ((IndexChange) => Callback) => (Cursor[R, P]) => JsComponent.Unmounted[js.Object, Null] = {
//    ListView[R, P, Cursor[A, Q]](
//      name,
//      listCursorToItems,
//      c => itemToKey(c.model),
//      itemView,
//      subheader,
//      mode
//    )
//  }
//
//  def usingRef[R, P, A, Q](
//    name: String,
//    rootToItemRefs: Cursor[R, P] => Cursor[List[Ref[A]], P],
//    itemAndCursorToAction: (A, Cursor[R, P]) => Q,
//    itemView: Cursor[A, Q] => VdomElement,
//    subheader: String,
//    mode: ListMode = ListMode.Infinite
//  )(implicit
//    fEncoder: Encoder[FindRefById[A]],
//    mCodec: MirrorCodec[A],
//    toId: Identifiable[A],
//    s: Searchable[A, Guid]
//  ): ((IndexChange) => Callback) => (Cursor[R, P]) => JsComponent.Unmounted[js.Object, Null] = {
//
//    ListView.withAction[R, P, A, Q](
//      name,
//      (cp: Cursor[R, P]) =>
//        rootToItemRefs(cp)
//          .zoomAllMatches(a => FindRefById(a.id))
//          .flatMap(cursorToRef => cursorToRef.followRef(cursorToRef.model))
//          .map(ca => ca.move(itemAndCursorToAction(ca.model, cp))),
//      a => toId.id(a).toString(),
//      itemView,
//      subheader,
//      mode
//    )
//  }
//
//  def usingMatches[R, P, A, Q, F <: A => Boolean](
//    name: String,
//    rootToItems: Cursor[R, P] => Cursor[List[A], P],
//    itemToFinder: A => F,
//    itemAndCursorToAction: (A, Cursor[R, P]) => Q,
//    itemToKey: A => js.Any,
//    itemView: Cursor[A, Q] => VdomElement,
//    subheader: String,
//    mode: ListMode = ListMode.Infinite
//  )(implicit
//    fEncoder: Encoder[F],
//    s: Searchable[A, Guid]
//  ): ((IndexChange) => Callback) => (Cursor[R, P]) => JsComponent.Unmounted[js.Object, Null] = {
//    ListView.withAction[R, P, A, Q](
//      name              = name,
//      listCursorToItems = (cp: Cursor[R, P]) => rootToItems(cp).zoomAllMatches(itemToFinder).map(ca => ca.move(itemAndCursorToAction(ca.model, cp))),
//      itemToKey         = c => itemToKey(c),
//      itemView          = itemView,
//      subheader         = subheader,
//      mode              = mode
//    )
//  }
//
//  def usingId[R, P, A <: Identified[A], Q](
//    name: String,
//    rootToItems: Cursor[R, P] => Cursor[List[A], P],
//    itemAndCursorToAction: (A, Cursor[R, P]) => Q,
//    itemView: Cursor[A, Q] => VdomElement,
//    subheader: String,
//    mode: ListMode = ListMode.Infinite
//  )(implicit
//    fEncoder: Encoder[FindById[A]],
//    s: Searchable[A, Guid]
//  ): ((IndexChange) => Callback) => (Cursor[R, P]) => JsComponent.Unmounted[js.Object, Null] = {
//    ListView.usingMatches[R, P, A, Q, FindById[A]](
//      name                  = name,
//      rootToItems           = rootToItems,
//      itemToFinder          = a => FindById[A](a.id),
//      itemAndCursorToAction = itemAndCursorToAction,
//      itemToKey             = a => a.id.toString(),
//      itemView              = itemView,
//      subheader             = subheader,
//      mode                  = mode
//    )
//  }
//
//  sealed trait ListMode
//  object ListMode {
//    case object Infinite extends ListMode
//    case object Finite extends ListMode
//  }
//
//  /**
//    * Create a component viewing a list
//    * @param name         The name of the component
//    * @param listCursorToItems  Produce a list of elements from the model
//    * @param itemToKey    Produce a key for each element
//    * @param itemView     A view for an element
//    * @param subheader    The subheader text to display in list
//    * @tparam L           The type of the list-like model
//    * @tparam P           The type P for CursorP
//    * @tparam A           The type of list element
//    * @return             A view of the list, with infinite scrolling, suitable for use in a SortableContainer
//    */
//  def apply[L, P, A](
//    name: String,
//    listCursorToItems: Cursor[L, P] => List[A],
//    itemToKey: A => js.Any,
//    itemView: GenericComponent[A, CtorType.Props, _],
//    subheader: String,
//    mode: ListMode = ListMode.Infinite
//  ): ((IndexChange) => Callback) => (Cursor[L, P]) => JsComponent.Unmounted[js.Object, Null] = {
//    val sortableElement = SortableElement.wrap(itemView)
//
//    mode match {
//      // Wrap in an Infinite for performance on long lists
//      case ListMode.Infinite =>
//        // Use a height view to get us the height of the rendered element as an extra part of prop.
//        // This lets us scale the Infinite list appropriately to fill space.
//        val view = CursorHeightView[L, P](name) {
//          (cp, height) =>
//            val h: Int = height.map(_.toInt).getOrElse(60)
//
//            // We need to apply a style by class to get the Infinite to be 100% height rather than the
//            // "height: containerHeight" inline style it sets on itself. This allows it to resize to fill
//            // available space, then be measured by Measure, which adjusts the containerHeight. This is
//            // neater than wrapping in a "height: 100%" div, and also works with react-sortable-hoc, which
//            // expects the top level component to be the one containing the sortable elements. Using a div
//            // breaks this and so breaks the nice feature where dragging to container edge starts scrolling.
//            Infinite(elementHeight = 60, containerHeight = h, className = "tree-infinite--height-100-percent")(
//              MuiSubheader(
//                inset = true,
//                style = js.Dynamic.literal(
//                  "height" -> "60px",
//                  "padding-top" -> "8px"
//                )
//              )(subheader: VdomNode) :: listCursorToItems(cp).zipWithIndex.map {
//                case (a, index) => sortableElement(SortableElement.Props(key = itemToKey(a), index = index))(a) : VdomElement
//              }
//            )
//        }
//        val sortableView = SortableContainer.wrap(view)
//        (onIndexChange: IndexChange => Callback) => sortableView(p(onIndexChange))
//
//      // Don't wrap with infinite, therefore doesn't need a height view.
//      // For this case we can also provide enter/leave transitions.
//      case ListMode.Finite =>
//        val view = cursorView[L, P](name) {
//          cp =>
//            CSSTransitionGroup(
//              "tree-list-view--transition",
//              enterTimeout = 250, // Animation should take 225ms
//              leaveTimeout = 220, // Animation should take 195ms
//              component = "div",
//              className = "tree-list-view__transition-container"
//            )(
//              (MuiSubheader(
//                inset = true,
//                style = js.Dynamic.literal(
//                  "height" -> "60px",
//                  "padding-top" -> "8px"
//                )
//              )(subheader: VdomNode):VdomNode) :: listCursorToItems(cp).zipWithIndex.map {
//                case (a, index) => sortableElement(SortableElement.Props(key = itemToKey(a), index = index))(a) : VdomNode
//              } : _*
//            )
//        }
//        val sortableView = SortableContainer.wrap(view)
//        (onIndexChange: IndexChange => Callback) => sortableView(p(onIndexChange))
//    }
//  }
//
//  def p(onSortEnd: IndexChange => Callback = p => Callback{}): SortableContainer.Props =
//    SortableContainer.Props(
//      onSortEnd = onSortEnd,
//      useDragHandle = true,
//      helperClass = "react-sortable-handler"
//    )
//
//}

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
    itemToKey: A => Key,
    itemView: Cursor[A, Pages[C, P]] => VdomElement,
    subheader: String,
    mode: ListMode = ListMode.Infinite
  )(implicit
    fEncoder: Encoder[F],
    s: Searchable[A, Guid]
  ): (IndexChange => Callback) => (Cursor[List[A], Pages[C, P]]) => VdomElement = {
    ListView[List[A], Pages[C, P], Cursor[A, Pages[C, P]]](
      name,
      _.zoomAllMatches(toItem),
      c => itemToKey(c.model),
      itemView,
      subheader,
      mode
    )
  }

  /**
    * Create a list view
    * @param name               Name of view
    * @param listCursorToItems  Take a cursor with root model (type R) and location as a page (type P) and yield a list
    *                           of cursors to the list items (type A) and locations as actions on them (type Q). Note we
    *                           are using the location parameter as an action to perform on list items.
    * @param itemToKey          Get a key for given list item
    * @param itemView           Get a view for Cursor[A, Q], which displays list items of type A, allowing actions to
    *                           be performed using location of type Q.
    * @param subheader          Subheader text for the list
    * @param mode               Mode for list display
    * @tparam R                 The type of root model - some data type from which we can get to a list of items - doesn't
    *                           need to actually be a list
    * @tparam P                 The type of location for the root cursor - normally some kind of "page" location
    * @tparam A                 The type of list items
    * @tparam Q                 The type of location for list items - often some kind of action that can be performed
    *                           on list items, but can be an actual location.
    * @return                   A List view
    */
  def withAction[R, P, A, Q](
    name: String,
    listCursorToItems: Cursor[R, P] => List[Cursor[A, Q]],
    itemToKey: A => Key,
    itemView: Cursor[A, Q] => VdomElement,
    subheader: String,
    mode: ListMode = ListMode.Infinite
  ): ((IndexChange) => Callback) => (Cursor[R, P]) => VdomElement = {
    ListView[R, P, Cursor[A, Q]](
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
    rootToItems: Cursor[R, P] => Cursor[List[A], P],
    itemToFinder: A => F,
    itemAndCursorToAction: (A, Cursor[R, P]) => Q,
    itemToKey: A => Key,
    itemView: Cursor[A, Q] => VdomElement,
    subheader: String,
    mode: ListMode = ListMode.Infinite
  )(implicit
    fEncoder: Encoder[F],
    s: Searchable[A, Guid]
  ): ((IndexChange) => Callback) => (Cursor[R, P]) => VdomElement = {
    ListView.withAction[R, P, A, Q](
      name              = name,
      listCursorToItems = (cp: Cursor[R, P]) => rootToItems(cp).zoomAllMatches(itemToFinder).map(ca => ca.move(itemAndCursorToAction(ca.model, cp))),
      itemToKey         = c => itemToKey(c),
      itemView          = itemView,
      subheader         = subheader,
      mode              = mode
    )
  }

  def usingId[R, P, A <: Identified[A], Q](
    name: String,
    rootToItems: Cursor[R, P] => Cursor[List[A], P],
    itemAndCursorToAction: (A, Cursor[R, P]) => Q,
    itemView: Cursor[A, Q] => VdomElement,
    subheader: String,
    mode: ListMode = ListMode.Infinite
  )(implicit
    s: Searchable[A, Guid]
  ): ((IndexChange) => Callback) => (Cursor[R, P]) => VdomElement =
    ListView.withAction[R, P, A, Q](
      name              = name,
      listCursorToItems = (cp: Cursor[R, P]) => rootToItems(cp).zoomAllIds.map(ca => ca.move(itemAndCursorToAction(ca.model, cp))),
      itemToKey         = a => a.id.toString(),
      itemView          = itemView,
      subheader         = subheader,
      mode              = mode
    )

  def usingRef[R, P, A, Q](
                            name: String,
                            rootToItemRefs: Cursor[R, P] => Cursor[List[Ref[A]], P],
                            itemAndCursorToAction: (A, Cursor[R, P]) => Q,
                            itemView: Cursor[A, Q] => VdomElement,
                            subheader: String,
                            mode: ListMode = ListMode.Infinite
                          )(implicit
                            mCodec: MirrorCodec[A],
                            // TODO use the id form the ref instead - then we won't need this Identifiable
                            toId: Identifiable[A],
                            s: Searchable[A, Guid]
                          ): ((IndexChange) => Callback) => (Cursor[R, P]) => VdomElement = {

    ListView.withAction[R, P, A, Q](
      name              = name,
      listCursorToItems = (cp: Cursor[R, P]) => rootToItemRefs(cp)
                                                  .zoomAllRefsById
                                                  .flatMap(cursorToRef => cursorToRef.followRef(cursorToRef.model))
                                                  .map(ca => ca.move(itemAndCursorToAction(ca.model, cp))),
      itemToKey         = a => toId.id(a).toString,
      itemView          = itemView,
      subheader         = subheader,
      mode              = mode
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
    listCursorToItems: Cursor[L, P] => List[A],
    itemToKey: A => Key,
    itemView: A => VdomElement,
    subheader: String,
    mode: ListMode = ListMode.Infinite
  ): ((IndexChange) => Callback) => (Cursor[L, P]) => VdomElement = {

    //FIXME restore infinite scrolling when Measure is fixed :)
    mode match {
      // Wrap in an Infinite for performance on long lists
      case ListMode.Infinite =>
        // Use a height view to get us the height of the rendered element as an extra part of prop.
        // This lets us scale the Infinite list appropriately to fill space.
        val view = CursorHeightView[L, P](name) {
          (cp, height) =>
            // Note we provide a minimum height of 1 since Infinite detects
            // a height of 0 as a missing property!
            val h: Int = Math.max(1, height.map(_.toInt).getOrElse(300))

            // We need to apply a style by class to get the Infinite to be 100% height rather than the
            // "height: containerHeight" inline style it sets on itself. This allows it to resize to fill
            // available space, then be measured by Measure, which adjusts the containerHeight. This is
            // neater than wrapping in a "height: 100%" div, and also works with react-sortable-hoc, which
            // expects the top level component to be the one containing the sortable elements. Using a div
            // breaks this and so breaks the nice feature where dragging to container edge starts scrolling.
            Infinite(elementHeight = 60, containerHeight = h, className = "tree-infinite--height-100-percent")(
              MuiSubheader(
                inset = true,
                style = js.Dynamic.literal(
                  "height" -> "60px",
                  "padding-top" -> "8px"
                )
              )(subheader: VdomNode) :: listCursorToItems(cp).map {
                a => <.div(^.className := "tree-list-view__item", ^.key := itemToKey(a), itemView(a)): VdomElement
              }
            )
        }
        (onIndexChange: IndexChange => Callback) => view(_)

      // Don't wrap with infinite, therefore doesn't need a height view.
      // For this case we can also provide enter/leave transitions.
      case ListMode.Finite =>
        val view = cursorView[L, P](name) {
          cp =>
            CSSTransitionGroup(
              "tree-list-view--transition",
              enterTimeout = 250, // Animation should take 225ms
              leaveTimeout = 220, // Animation should take 195ms
              component = "div",
              className = "tree-list-view__transition-container"
            )(
              (MuiSubheader(
                inset = true,
                style = js.Dynamic.literal(
                  "height" -> "60px",
                  "padding-top" -> "8px"
                )
              )(subheader: VdomNode):VdomNode) :: listCursorToItems(cp).map {
                a => <.div(^.className := "tree-list-view__item", ^.key := itemToKey(a), itemView(a)): VdomElement
              } : _*
            )
        }
        (onIndexChange: IndexChange => Callback) => view(_)
    }
  }

//  def p(onSortEnd: IndexChange => Callback = p => Callback{}): SortableContainer.Props =
//    SortableContainer.Props(
//      onSortEnd = onSortEnd,
//      useDragHandle = true,
//      helperClass = "react-sortable-handler"
//    )

}
