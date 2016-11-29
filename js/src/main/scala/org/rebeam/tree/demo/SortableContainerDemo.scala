package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.sortable._
import org.rebeam.tree.view._

import scala.scalajs.js

object SortableContainerDemo {

  class Backend(scope: BackendScope[Unit, List[String]]) {

    def itemView(item: String) = View.staticView("ItemView")(<.li(item))

    def render(props: Unit, items: List[String]) = {
      SortableContainer(
        onSortEnd = p => scope.modState(s => s)
      )
      (
        <.ul(
          items.zipWithIndex.map{ case (item, index) => SortableElement(index = index)(itemView(item))}
        )
      )
    }

  }

  val defaultItems = Range(0, 10).map("Item " + _).toList

  val c = ReactComponentB[Unit]("SortableContainerDemo")
    .initialState(defaultItems)
    .backend(new Backend(_))
    .render(s => s.backend.render(s.props, s.state))
    .build

}
