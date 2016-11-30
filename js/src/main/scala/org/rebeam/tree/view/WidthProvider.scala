package org.rebeam.tree.view

import japgolly.scalajs.react._

import scala.scalajs.js

case class WidthProvider[P](measureBeforeMount: Boolean = true)(wrappedProps: P) {

  def toJS = {
    val p = js.Dynamic.literal()
    p.updateDynamic("measureBeforeMount")(measureBeforeMount)
    p.updateDynamic("v")(wrappedProps.asInstanceOf[js.Any])
    p
  }

  def apply(wrappedComponent: ReactComponentC[P,_,_,_]) = {

    //SortableElement is a HOC, so we pass it a wrapped component to get back a SortableElement component
    val componentFactory = js.Dynamic.global.WidthProvider(wrappedComponent.factory)

    //Now we use that component to make a factory
    val component = React.asInstanceOf[js.Dynamic].createFactory(componentFactory)

    //Finally pass the props as JS to the factory to make an unmounted react component
    component(toJS).asInstanceOf[ReactComponentU_]
  }

}
