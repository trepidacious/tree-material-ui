package org.rebeam.tree.view.sortable

import japgolly.scalajs.react._

import scala.scalajs.js

object SortableElement {
  case class Props(index: Int,
                   collection: Int = 0,
                   disabled: Boolean = false)

  /**
    * Wrap another component
    * @param wrappedComponent The wrapped component itself
    * @tparam P               The type of Props of the wrapped component
    * @return                 A component wrapping the wrapped component...
    */
  def wrap[P](wrappedComponent: ReactComponentC[P,_,_,_]): Props => P => ReactComponentU_ = {

    val componentFactoryFunction = js.Dynamic.global.SortableElement(wrappedComponent.factory)
    val componentFactory = React.asInstanceOf[js.Dynamic].createFactory(componentFactoryFunction)

    (props) => (wrappedProps) => componentFactory(js.Dynamic.literal(
      "index" -> props.index,
      "collection" -> props.collection,
      "disabled" -> props.disabled,
      // Props of scala react components use a single "v" field containing the
      // actual property value. The HOC will pass this through to the wrapped
      // component, in addition to the "width" property it inserts in.
      "v" -> wrappedProps.asInstanceOf[js.Any]
    )).asInstanceOf[ReactComponentU_]
  }
}

//case class SortableElement(
//  index: Int,
//  collection: Int = 0,
//  disabled: Boolean = false) {
//
//  def toJS = {
//    val p = js.Dynamic.literal()
//    p.updateDynamic("index")(index)
//    p.updateDynamic("collection")(collection)
//    p.updateDynamic("disabled")(disabled)
//    p
//  }
//
//  def apply(wrappedComponent: ReactComponentC[_,_,_,_]) = {
//
//    //SortableElement is a HOC, so we pass it a wrapped component to get back a SortableElement component
//    val componentFactory = js.Dynamic.global.SortableElement(wrappedComponent.factory)
//
//    //Now we use that component to make a factory
//    val component = React.asInstanceOf[js.Dynamic].createFactory(componentFactory)
//
//    //Finally pass the props as JS to the factory to make an unmounted react component
//    component(toJS).asInstanceOf[ReactComponentU_]
//  }
//
//}
