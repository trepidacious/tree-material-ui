//package org.rebeam.tree.view.list
//
//import japgolly.scalajs.react._
//
//import scala.scalajs.js
//
//object SortableElement {
//
//  case class Props(key: js.Any,
//                   index: Int,
//                   collection: Int = 0,
//                   disabled: Boolean = false)
//
//  /**
//    * Wrap another component
//    * @param wrappedComponent The wrapped component itself
//    * @tparam P               The type of Props of the wrapped component
//    * @return                 A component wrapping the wrapped component...
//    */
//  def wrap[P](wrappedComponent: GenericComponent[P, _, _]): Props => P => GenericComponent.UnmountedRaw = {
//
//
//    //Need a raw.ReactCtor
////    raw.React.createElement()
//
////    val component = js.Dynamic.global.SortableElement(wrappedComponent.reactClass)
//    val component = js.Dynamic.global.SortableElement(wrappedComponent.raw)
//
//    // The "v" prop is probably not right any more
//    (props) => (wrappedProps) => js.Dynamic.global.React.createElement(component, js.Dynamic.literal(
//      "key" -> props.key,
//      "index" -> props.index,
//      "collection" -> props.collection,
//      "disabled" -> props.disabled,
//      "v" -> wrappedProps.asInstanceOf[js.Any]
//    )).asInstanceOf[GenericComponent.UnmountedRaw]
//  }
//}
