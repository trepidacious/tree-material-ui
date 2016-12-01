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
  def wrap[P](wrappedComponent: ReactComponentC[P,_,_,_]): Props => P => ReactComponentU_ = {

    //Some of the terminology here might be incorrect...

    //WidthProvider is a HOC, so we pass it our wrapped component's factory function
    //to produce a factory function for our output component
    val componentFactoryFunction = js.Dynamic.global.WidthProvider(wrappedComponent.factory)

    //Now we use that factory function to make a factory
    val componentFactory = React.asInstanceOf[js.Dynamic].createFactory(componentFactoryFunction)

    //Finally we make the factory back into a scala function. This accepts
    //props for the WidthProvider itself and for the wrapped component, and
    //assembles them into a combined JS props object. This can then be passed
    //to our componentFactory to yield a final component. This will act like the
    //wrapped component, but with a "width" property added. Unfortunately this
    //is a little pointless since the scala component can't access the width
    //property (it is outside the "v" field used to store the scala Props object)
    //but it demonstrates the principle. For a HOC that does something other than
    //intercepting props this would work fine.
    (props) => (wrappedProps) => componentFactory(js.Dynamic.literal(
      "measureBeforeMount" -> props.measureBeforeMount,
      // Props of scala react components use a single "v" field containing the
      // actual property value. The HOC will pass this through to the wrapped
      // component, in addition to the "width" property it inserts in.
      "v" -> wrappedProps.asInstanceOf[js.Any]
    )).asInstanceOf[ReactComponentU_]
  }
}