package org.rebeam.tree.view

import japgolly.scalajs.react._

import scala.scalajs.js

object WidthProvider {

  //Props for the HOC itself
  case class Props(measureBeforeMount: Boolean = true)

  /**
    * Wrap another component
    * @param wrappedComponent The wrapped component itself
    * @tparam P               The type of Props of the wrapped component
    * @return                 A component wrapping the wrapped component...
    */
  def wrap[P](wrappedComponent: GenericComponent[P, CtorType.Props, _]): Props => P => JsComponent.Unmounted[js.Object, Null] = {

    //Some of the terminology here might be incorrect...

    //WidthProvider is a HOC, so we pass it our wrapped component's factory function
    //to produce a factory function for our output component
    // TODO is `.raw` really want we want?
    val componentFactoryFunction = js.Dynamic.global.WidthProvider(wrappedComponent.raw)

    //Then we make a JsComponent from that
    val component = JsComponent[js.Object, Children.None, Null](componentFactoryFunction)

    //Finally we make the component back into a scala function. This accepts
    //props for the WidthProvider itself, then for the wrapped component, and
    //assembles them into a combined JS props object. This can then be passed
    //to our component to yield an unmounted component.
    //This will act like the wrapped component, but with a "width" field added to props.
    //Unfortunately this is a little pointless since the scala component can't access
    //the width property (it is outside the "a" field used to store the scala Props
    //object) but it demonstrates the principle. For a HOC that does something other
    //than inserting prop fields this would work fine.
    (props) => (wrappedProps) =>
      component(js.Dynamic.literal(
      "measureBeforeMount" -> props.measureBeforeMount,
      // Props of scala react components use a single "a" field containing the
      // actual property value, see japgolly.scalajs.react.internal.Box.
      // The HOC will pass this through to the wrapped component, in addition
      // to the "width" property it inserts in.
      // TODO use Box to wrap rather than explicit `a` field?
      "a" -> wrappedProps.asInstanceOf[js.Any]
    ))
  }
}