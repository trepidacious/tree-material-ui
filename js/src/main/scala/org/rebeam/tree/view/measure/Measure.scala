package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomNode
import org.rebeam.tree.view.measure.Measure.{Dimensions, DimensionsJS}

import scala.scalajs.js

object Measure {

  case class Dimensions(width: Double, height: Double)

  @js.native
  trait DimensionsJS extends js.Object {
    val width: Double = js.native
    val height: Double = js.native
  }

  def height(onMeasure: Dimensions => Callback)(child : VdomNode): JsComponent.Unmounted[Props, Null] = Measure(whitelist = List("height"), shouldMeasure = true, onMeasure = onMeasure)(child)

  @js.native
  trait Props extends js.Object {
    var whitelist: js.Array[String] = js.native
    var blacklist: js.Array[String] = js.native
    var includeMargin: js.UndefOr[Boolean] = js.native
    var useClone: js.UndefOr[Boolean] = js.native
    var shouldMeasure: js.UndefOr[Boolean] = js.native
    var onMeasure: DimensionsJS => Unit = js.native
  }

  private val rawComponent = js.Dynamic.global.Measure
  private val component = JsComponent[Props, Children.Varargs, Null](rawComponent)

  def apply(
    whitelist: List[String] = Nil,
    blacklist: List[String] = Nil,
    includeMargin: js.UndefOr[Boolean] = js.undefined,
    useClone: js.UndefOr[Boolean] = js.undefined,
    shouldMeasure: js.UndefOr[Boolean] = js.undefined,
    onMeasure: Dimensions => Callback
  )(child : VdomNode): JsComponent.Unmounted[Props, Null] = {

    val p = (new js.Object).asInstanceOf[Props]

    import js.JSConverters._
    p.whitelist = whitelist.toJSArray
    p.blacklist = blacklist.toJSArray
    p.includeMargin = includeMargin
    p.useClone = useClone
    p.shouldMeasure = shouldMeasure
    val onMeasureJS: (DimensionsJS) => Unit = djs => {
      val d = Dimensions(djs.width, djs.height)
      onMeasure(d).runNow()
    }
    p.onMeasure = onMeasureJS

    component(p)(child)
  }


}
