//package org.rebeam.tree.view.list
//
//import japgolly.scalajs.react._
//
//import scala.scalajs.js
//import scala.language.higherKinds
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
//  def wrap[P](wrappedComponent: GenericComponent[P, CtorType.Props, _]): Props => P => JsComponent.Unmounted[js.Object, Null] = {
//
//    //Need a raw.ReactCtor
////    raw.React.createElement()
//
////    val component = js.Dynamic.global.SortableElement(wrappedComponent.reactClass)
//    val componentFactoryFunction = js.Dynamic.global.SortableElement(wrappedComponent.raw)
//    val component = JsComponent[js.Object, Children.None, Null](componentFactoryFunction)
//
//    // The "v" prop is probably not right any more
//    (props) => (wrappedProps) => component(
//      js.Dynamic.literal(
//        "key" -> props.key,
//        "index" -> props.index,
//        "collection" -> props.collection,
//        "disabled" -> props.disabled,
//        "a" -> wrappedProps.asInstanceOf[js.Any]
//      )
//    )
//  }
//}
