package org.rebeam.tree.demo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.sortable._

object SortableContainerDemo {

  // Equivalent of ({value}) => <li>{value}</li> in original demo
  val liView = ReactComponentB[String]("liView")
    .render(d => {
      <.li(s"${d.props}")
    })
    .build

  // As in original demo
  val sortableItem = SortableElement.wrap(liView)

  // Equivalent of the `({items}) =>` lambda passed to SortableContainer in original demo
  val ulView = ReactComponentB[List[String]]("ulView")
    .render(d => {
      <.ul(
        d.props.zipWithIndex.map {
          case (value, index) =>
            sortableItem(SortableElement.Props(index = index))(value)
        }
      )
    })
    .build

  // As in original demo
  val sortableList = SortableContainer.wrap(ulView)

  // As in original SortableComponent
  class Backend(scope: BackendScope[Unit, List[String]]) {
    def render(props: Unit, items: List[String]) = {
      sortableList(
        SortableContainer.Props(
          onSortEnd = p =>
            scope.modState(
              l => p.updatedList(l)
            ) >> Callback{println(p)}
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
