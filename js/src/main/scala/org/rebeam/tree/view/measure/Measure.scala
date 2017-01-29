package org.rebeam.tree.view.measure

import japgolly.scalajs.react._
import org.rebeam.tree.view.measure.Measure.{Dimensions, DimensionsJS}

import scala.scalajs.js

object Measure {

  case class Dimensions(width: Double, height: Double)

  @js.native
  trait DimensionsJS extends js.Object {
    val width: Double                       = js.native
    val height: Double                      = js.native
  }

}

case class Measure(
  whitelist: List[String] = Nil,
  blacklist: List[String] = Nil,
  includeMargin: js.UndefOr[Boolean] = js.undefined,
  useClone: js.UndefOr[Boolean] = js.undefined,
  shouldMeasure: js.UndefOr[Boolean] = js.undefined,
  onMeasure: Dimensions => Callback
) {

  private def toJS = {
    import js.JSConverters._
    val p = js.Dynamic.literal()
    p.updateDynamic("whitelist")(whitelist.toJSArray)
    p.updateDynamic("blacklist")(blacklist.toJSArray)
    includeMargin.foreach(p.updateDynamic("includeMargin")(_))
    useClone.foreach(p.updateDynamic("useClone")(_))
    shouldMeasure.foreach(p.updateDynamic("shouldMeasure")(_))

    val onMeasureJS: (DimensionsJS) => Unit = djs => {
      val d = Dimensions(djs.width, djs.height)
      onMeasure(d).runNow()
    }


    p.updateDynamic("onMeasure")(onMeasureJS)

    p
  }

  def apply(child : ReactNode): ReactComponentU_ = {
    val f = React.asInstanceOf[js.Dynamic].createFactory(js.Dynamic.global.Measure) // access real js component
    f(toJS, child).asInstanceOf[ReactComponentU_]
  }
}

