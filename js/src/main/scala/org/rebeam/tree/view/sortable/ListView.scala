package org.rebeam.tree.view.sortable

import chandu0101.scalajs.react.components.materialui._
import io.circe.Encoder
import japgolly.scalajs.react.ReactComponentC.ReqProps
import japgolly.scalajs.react.{Callback, TopNode}
import org.rebeam.tree.view.CursorP
import org.rebeam.tree.view.infinite.Infinite
import org.rebeam.tree.view.measure.CursorPHeightView
import org.rebeam.tree.view.pages.Pages
import org.rebeam.tree.view.sortable.SortableContainer.Props

import scala.scalajs.js

object ListView {

  /**
    * Create a component viewing a list
    * @param name       The name of the component
    * @param toItem     Function from an item to a finder, used for CursorP.zoomAllMatchesP
    * @param itemToKey  Maps items to their react keys
    * @param itemView   Component viewing items
    * @param subheader  The subheader text to display in list
    * @param fEncoder   Encoder for finder
    * @tparam A         The type of list element
    * @tparam C         The type of current page
    * @tparam P         The type of all pages
    * @tparam F         The type of finder used to find items
    * @return           A view of the list, with infinite scrolling, suitable for use in a SortableContainer
    */
  def apply[A, C, P, F <: A => Boolean](
      name: String,
      toItem: A => F,
      itemToKey: A => js.Any,
      itemView: ReqProps[CursorP[A, Pages[C, P]], Unit, Unit, TopNode],
      subheader: String)(implicit fEncoder: Encoder[F]) =
    {
      val sortableElement = SortableElement.wrap(itemView)

      // Use a height view to get us the height of the rendered element as an extra part of prop.
      // This lets us scale the Infinite list appropriately to fill space.
      val view = CursorPHeightView[List[A], Pages[C, P]](name) {
        (cp, height) =>
          val h: Int = height.map(_.toInt).getOrElse(60)

          // We need to apply a style by class to get the Infinite to be 100% height rather than the
          // "height: containerHeight" inline style it sets on itself. This allows it to resize to fill
          // available space, then be measured by Measure, which adjusts the containerHeight. This is
          // neater than wrapping in a "height: 100%" div, and also works with react-sortable-hoc, which
          // expects the top level component to be the one containing the sortable elements. Using a div
          // breaks this and so breaks the nice feature where dragging to container edge starts scrolling.
          Infinite(elementHeight = 60, containerHeight = h, className = "tree-infinite--height-100-percent")(
            MuiSubheader(inset = true, style = js.Dynamic.literal("height" -> "60px"))(subheader)
              :: cp.zoomAllMatchesP(toItem).zipWithIndex.map {
              case (itemCP, index) => sortableElement(SortableElement.Props(key = itemToKey(itemCP.model), index = index))(itemCP)
            }
          )
      }
      val sortableView = SortableContainer.wrap(view)
      (onIndexChange: IndexChange => Callback) => sortableView(p(onIndexChange))
    }

  def p(onSortEnd: IndexChange => Callback = p => Callback{}): SortableContainer.Props =
    SortableContainer.Props(
      onSortEnd = onSortEnd,
      useDragHandle = true,
      helperClass = "react-sortable-handler"
    )

}
