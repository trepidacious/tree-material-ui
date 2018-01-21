package org.rebeam.tree.view.list

import japgolly.scalajs.react._

import scala.scalajs.js

object SortableContainer {
  @js.native
  trait PermutationJS extends js.Object {
    val oldIndex: Int                              = js.native
    val newIndex: Int                              = js.native
    //Could have collection as well
  }

//  @js.native
//  trait PropsJS extends js.Object {
//    var axis: js.UndefOr[String] = js.native
//    var lockAxis: js.UndefOr[String] = js.native
//    var helperClass: js.UndefOr[String] = js.native
//    var transitionDuration: js.UndefOr[Int] = js.native
//    var pressDelay: js.UndefOr[Int] = js.native
//    var distance: js.UndefOr[Int] = js.native
//    //var shouldCancelStart <- undef or a function from event to Boolean
//
//    var useDragHandle: js.UndefOr[Boolean] = js.native
//    var useWindowAsScrollContainer: js.UndefOr[Boolean] = js.native
//    var hideSortableGhost: js.UndefOr[Boolean] = js.native
//    var lockToContainerEdges: js.UndefOr[Boolean] = js.native
//    //var lockOffset <- really not sure what this is from docs - maybe a string like "50%"?
//    //var getContainer <- undef or function returning scrollable container element, function(wrappedInstance: React element): DOM element.
//    //var getHelperDimensions <- undef or function({node, index, collection})
//
//    //Note this function actually gets "{oldIndex, newIndex, collection}, e", but we don't have much use for the other arguments
//    var onSortEnd: js.Function1[IndexChange, Unit] = js.native
//    //var onSortStart <- undef or function({node, index, collection}, event)
//    //var onSortMove <- undef or function(event)
//  }


  case class Props(
    axis: js.UndefOr[String] = js.undefined,
    lockAxis: js.UndefOr[String] = js.undefined,
    helperClass: js.UndefOr[String] = js.undefined,
    transitionDuration: js.UndefOr[Int] = js.undefined,
    pressDelay: js.UndefOr[Int] = js.undefined,
    distance: js.UndefOr[Int] = js.undefined,
    //shouldCancelStart <- undef or a function from event to Boolean

    useDragHandle: js.UndefOr[Boolean] = js.undefined,
    useWindowAsScrollContainer: js.UndefOr[Boolean] = js.undefined,
    hideSortableGhost: js.UndefOr[Boolean] = js.undefined,
    lockToContainerEdges: js.UndefOr[Boolean] = js.undefined,
    //lockOffset <- really not sure what this is from docs - maybe a string like "50%"?
    //getContainer <- undef or function returning scrollable container element, function(wrappedInstance: React element): DOM element.
    //getHelperDimensions <- undef or function({node, index, collection})

    //Note this function actually gets "{oldIndex, newIndex, collection}, e", but we don't have much use for the other arguments
    onSortEnd: IndexChange => Callback = p => Callback{}
    //onSortStart <- undef or function({node, index, collection}, event)
    //onSortMove <- undef or function(event)
  ) {
    private[list] def toJS = {
      val p = js.Dynamic.literal()
      axis.foreach(p.updateDynamic("axis")(_))
      lockAxis.foreach(p.updateDynamic("lockAxis")(_))
      helperClass.foreach(p.updateDynamic("helperClass")(_))
      transitionDuration.foreach(p.updateDynamic("transitionDuration")(_))
      pressDelay.foreach(p.updateDynamic("pressDelay")(_))
      distance.foreach(p.updateDynamic("distance")(_))
      useDragHandle.foreach(p.updateDynamic("useDragHandle")(_))
      useWindowAsScrollContainer.foreach(p.updateDynamic("useWindowAsScrollContainer")(_))
      hideSortableGhost.foreach(p.updateDynamic("hideSortableGhost")(_))
      lockToContainerEdges.foreach(p.updateDynamic("lockToContainerEdges")(_))

      val onSortEndJS: js.Function1[PermutationJS, Unit] = pjs => {
        val ic = IndexChange(pjs.oldIndex, pjs.newIndex)
        onSortEnd(ic).runNow()
      }

      p.updateDynamic("onSortEnd")(onSortEndJS)

      //TODO other callbacks

      p
    }
  }

  /**
    * Wrap another component
    * @param wrappedComponent The wrapped component itself
    * @tparam P               The type of Props of the wrapped component
    * @return                 A component wrapping the wrapped component...
    */
  def wrap[P](wrappedComponent: GenericComponent[P, CtorType.Props, _]):
    Props => P => JsComponent.Unmounted[js.Object, Null] = {

    val componentFactoryFunction = js.Dynamic.global.SortableContainer(wrappedComponent.raw)

    val component = JsComponent[js.Object, Children.None, Null](componentFactoryFunction)

    (props) => (wrappedProps) => {
      val p = props.toJS
      p.updateDynamic("a")(wrappedProps.asInstanceOf[js.Any])
      component(p)
    }
  }

}


