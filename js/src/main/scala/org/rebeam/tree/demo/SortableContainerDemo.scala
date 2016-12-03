package org.rebeam.tree.demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.View
import org.rebeam.tree.view.sortable._

object SortableContainerDemo {

  import japgolly.scalajs.react.vdom.SvgTags._
  import japgolly.scalajs.react.vdom.SvgAttrs._

  val handle = View.staticView("handle")(
    <.div(
      ^.className := "react-sortable-handle",
      svg(
        ^.className := "react-sortable-handle-svg",
        viewBox := "0 0 24 24",
        path(d := "M9,8c1.1,0,2-0.9,2-2s-0.9-2-2-2S7,4.9,7,6S7.9,8,9,8z M9,10c-1.1,0-2,0.9-2,2s0.9,2,2,2s2-0.9,2-2S10.1,10,9,10z M9,16c-1.1,0-2,0.9-2,2s0.9,2,2,2s2-0.9,2-2S10.1,16,9,16z"),
        path(d := "M15,8c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S13.9,8,15,8z M15,10c-1.1,0-2,0.9-2,2s0.9,2,2,2s2-0.9,2-2S16.1,10,15,10z M15,16c-1.1,0-2,0.9-2,2s0.9,2,2,2s2-0.9,2-2S16.1,16,15,16z")
      )
    )
  )

  val sortableHandle = SortableHandle.wrap(handle)(())

  // Equivalent of ({value}) => <li>{value}</li> in original demo
  val itemView = ReactComponentB[String]("liView")
    .render(d => {
      <.div(
        ^.className := "react-sortable-item",
        sortableHandle,
        <.span(s"${d.props}")
      )
    })
    .build

  // As in original demo
  val sortableItem = SortableElement.wrap(itemView)

  // Equivalent of the `({items}) =>` lambda passed to SortableContainer in original demo
  val listView = ReactComponentB[List[String]]("listView")
    .render(d => {
      <.div(
        ^.className := "react-sortable-list",
        d.props.zipWithIndex.map {
          case (value, index) =>
            sortableItem(SortableElement.Props(index = index))(value)
        }
      )
    })
    .build

  // As in original demo
  val sortableList = SortableContainer.wrap(listView)

  // As in original SortableComponent
  class Backend(scope: BackendScope[Unit, List[String]]) {
    def render(props: Unit, items: List[String]) = {
      sortableList(
        SortableContainer.Props(
          onSortEnd = p =>
            scope.modState(
              l => p.updatedList(l)
            ),
          useDragHandle = true,
          helperClass = "react-sortable-handler"
        )
      )(items)
    }
  }

  val defaultItems = Range(0, 10).map("Item " + _).toList

  val c = ReactComponentB[Unit]("SortableContainerDemo")
    .initialState(defaultItems)
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
