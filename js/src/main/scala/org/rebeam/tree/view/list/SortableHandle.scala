package org.rebeam.tree.view.list

import japgolly.scalajs.react._

import scala.scalajs.js

object SortableHandle {
  /**
    * Wrap another component
    * @param wrappedComponent The wrapped component itself
    * @tparam P               The type of Props of the wrapped component
    * @return                 A component wrapping the wrapped component
    */
  def wrap[P](wrappedComponent: GenericComponent[P, CtorType.Props, _]): P => JsComponent.Unmounted[js.Object, Null] = {

    val componentFactoryFunction = js.Dynamic.global.SortableHandle(wrappedComponent.raw)

    val component = JsComponent[js.Object, Children.None, Null](componentFactoryFunction)

    (wrappedProps) => component(js.Dynamic.literal(
      "a" -> wrappedProps.asInstanceOf[js.Any]
    ))
  }
}
