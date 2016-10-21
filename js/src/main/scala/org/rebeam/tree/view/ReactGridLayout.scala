package org.rebeam.tree.view

import japgolly.scalajs.react.{Callback, React, ReactComponentU_, ReactNode}
import org.rebeam.tree.view.ReactGridLayout._
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js

object ReactGridLayout {

  @js.native
  trait LayoutItemJS extends js.Object {
    val i: String                           = js.native
    val x: Int                              = js.native
    val y: Int                              = js.native
    val w: Int                              = js.native
    val h: Int                              = js.native
    val minW: js.UndefOr[Int]               = js.native
    val maxW: js.UndefOr[Int]               = js.native
    val minH: js.UndefOr[Int]               = js.native
    val maxH: js.UndefOr[Int]               = js.native
    val static: js.UndefOr[Boolean]         = js.native
    val isDraggable: js.UndefOr[Boolean]    = js.native
    val isResizable: js.UndefOr[Boolean]    = js.native
  }

  case class LayoutItem(
    i: String,
    x: Int,
    y: Int,
    w: Int                              = 1,
    h: Int                              = 1,
    minW: js.UndefOr[Int]               = js.undefined,
    maxW: js.UndefOr[Int]               = js.undefined,
    minH: js.UndefOr[Int]               = js.undefined,
    maxH: js.UndefOr[Int]               = js.undefined,
    static: js.UndefOr[Boolean]         = js.undefined,
    isDraggable: js.UndefOr[Boolean]    = js.undefined,
    isResizable: js.UndefOr[Boolean]    = js.undefined
  ) {
    def toJS = {
      val p = js.Dynamic.literal()
      p.updateDynamic("i")(i)
      p.updateDynamic("x")(x)
      p.updateDynamic("y")(y)
      p.updateDynamic("w")(w)
      p.updateDynamic("h")(h)
      minW.foreach(p.updateDynamic("minW")(_))
      maxW.foreach(p.updateDynamic("maxW")(_))
      minH.foreach(p.updateDynamic("minH")(_))
      maxH.foreach(p.updateDynamic("maxH")(_))
      static.foreach(p.updateDynamic("static")(_))
      isDraggable.foreach(p.updateDynamic("isDraggable")(_))
      isResizable.foreach(p.updateDynamic("isResizable")(_))
      p
    }
  }

  type Layout = List[LayoutItem]

  case class XY(x: Int, y: Int) {
    def toJS = js.Array(x, y)
  }

  case class ItemUpdate(
    layout: Layout,
    oldItem: LayoutItem,
    newItem: LayoutItem,
    placeholder: LayoutItem,
    e: MouseEvent,
    element: HTMLElement
  )

  type ItemCallback = ItemUpdate => Unit

  val noOpItemCallback: ItemCallback = i => ()

//  val noOpOnLayoutChange: js.Any => Unit = l => {println(JSON.stringify(l))}
  val noOpOnLayoutChange: Layout => Callback = l => Callback{}

}

/**
  * Create a ReactGridLayout
  *
  * @param width            This allows setting the initial width on the server side.
  *                         This is required unless using the HOC WidthProvider or similar.
  * @param autoSize         If true, the container height swells and contracts to fit contents
  * @param cols             Number of columns in this layout.
  * @param draggableCancel  A CSS selector for tags that will not be draggable.
  *                         For example: draggableCancel:'.MyNonDraggableAreaClassName'
  *                         If you forget the leading . it will not work.
  * @param draggableHandle  A CSS selector for tags that will act as the draggable handle.
  *                         For example: draggableHandle:'.MyDragHandleClassName'
  *                         If you forget the leading . it will not work.
  * @param verticalCompact  If true, the layout will compact vertically
  * @param layout           Layout is a sequence of LayoutItem
  * @param margin           Margin between items in px, defaults to 10, 10
  * @param containerPadding Padding inside the container in px, defaults to margin
  * @param rowHeight        Rows have a static height, but you can change this based on breakpoints if you like,
  *                         defaults to 150
  * @param isDraggable      Whether dragging is supported, defaults to true
  * @param isResizable      Whether resizing is supported, defaults to true
  * @param useCSSTransforms Uses CSS3 translate() instead of position top/left.
  *                         This makes about 6x faster paint performance.
  *                         Defaults to true
  * @param onLayoutChange   Callback so you can save the layout
  *                         Calls back with (currentLayout) after every drag or resize stop.
  */
//  * @param onDragStart      Calls when drag starts.
//  * @param onDrag           Calls on each drag movement.
//  * @param onDragStop       Calls when drag is complete.
//  * @param onResizeStart    Calls when resize starts.
//  * @param onResize         Calls when resize movement happens.
//  * @param onResizeStop     Calls when resize is complete.
//  */
case class ReactGridLayout(
  width: Int                              = 500,
  autoSize: js.UndefOr[Boolean]           = js.undefined,
  cols: js.UndefOr[Int]                   = js.undefined,
  draggableCancel: js.UndefOr[String]     = js.undefined,
  draggableHandle: js.UndefOr[String]     = js.undefined,
  verticalCompact: js.UndefOr[Boolean]    = js.undefined,
  layout: js.UndefOr[Layout]              = js.undefined,
  margin: js.UndefOr[XY]                  = js.undefined,
  containerPadding: js.UndefOr[XY]        = js.undefined,
  rowHeight: js.UndefOr[Int]              = js.undefined,
  isDraggable: js.UndefOr[Boolean]        = js.undefined,
  isResizable: js.UndefOr[Boolean]        = js.undefined,
  useCSSTransforms: js.UndefOr[Boolean]   = js.undefined,
  onLayoutChange: Layout => Callback      = noOpOnLayoutChange
//  onDragStart: ItemCallback               = noOpItemCallback,
//  onDrag: ItemCallback                    = noOpItemCallback,
//  onDragStop: ItemCallback                = noOpItemCallback,
//  onResizeStart: ItemCallback             = noOpItemCallback,
//  onResize: ItemCallback                  = noOpItemCallback,
//  onResizeStop: ItemCallback              = noOpItemCallback
) {
  def toJS = {
    import js.JSConverters._

    val p = js.Dynamic.literal()
    p.updateDynamic("width")(width)
    autoSize.foreach(p.updateDynamic("autoSize")(_))
    cols.foreach(p.updateDynamic("cols")(_))
    draggableCancel.foreach(p.updateDynamic("draggableCancel")(_))
    draggableHandle.foreach(p.updateDynamic("draggableHandle")(_))
    verticalCompact.foreach(p.updateDynamic("verticalCompact")(_))
    layout.foreach(l => p.updateDynamic("layout")(l.map(_.toJS).toJSArray))
    margin.foreach(xy => p.updateDynamic("margin")(xy.toJS))
    containerPadding.foreach(xy => p.updateDynamic("containerPadding")(xy.toJS))
    rowHeight.foreach(p.updateDynamic("rowHeight")(_))
    isDraggable.foreach(p.updateDynamic("isDraggable")(_))
    isResizable.foreach(p.updateDynamic("isResizable")(_))
    useCSSTransforms.foreach(p.updateDynamic("useCSSTransforms")(_))

    def liFromJS(li: LayoutItemJS) = LayoutItem(li.i, li.x, li.y, li.w, li.h, li.minW, li.maxW, li.minH, li.maxH, li.static, li.isDraggable, li.isResizable)

    //TODO is it safe to runNow()?
    val onLayoutChangeJS: (js.Array[LayoutItemJS]) => Unit = l => {
      onLayoutChange(l.map(liFromJS).toList).runNow()
    }

    p.updateDynamic("onLayoutChange")(onLayoutChangeJS)

    //TODO other callbacks

    p
  }

  def apply(children : ReactNode*) = {
    val f = React.asInstanceOf[js.Dynamic].createFactory(js.Dynamic.global.ReactGridLayout) // access real js component
    f(toJS, children.toJsArray).asInstanceOf[ReactComponentU_]
  }
}


