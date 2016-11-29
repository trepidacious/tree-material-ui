package org.rebeam.tree.view.sortable

import japgolly.scalajs.react._

import scala.scalajs.js

object SortableContainer {
  @js.native
  trait PermutationJS extends js.Object {
    val oldIndex: Int                              = js.native
    val newIndex: Int                              = js.native
    //Could have collection as well
  }
}

/**
  * Create a SortableContainer
  *
  * @param axis The axis you want to sort on, either 'x' or 'y'
  * @param lockAxis If you'd like, you can lock movement to an axis while sorting.
  *                 This is not something that is possible with HTML5 Drag & Drop.
  *                 Either 'x' or 'y'
  * @param helperClass You can provide a class you'd like to add to the sortable
  *                    helper to add some styles to it
  * @param transitionDuration The duration in ms of the transition when elements shift positions.
  *                           Set this to 0 if you'd like to disable transitions.
  *                           Default 300.
  * @param pressDelay If you'd like elements to only become sortable after being pressed for a
  *                   certain time, change this property. A good sensible default value for mobile
  *                   is 200. Cannot be used in conjunction with the distance prop.
  *                   Default 0.
  * @param distance If you'd like elements to only become sortable after being
  *                 dragged a certain number of pixels. Cannot be used in conjunction
  *                 with the pressDelay prop.
  *                 Default 0.
  * @param useDragHandle If you're using the SortableHandle HOC, set this to true.
  *                      Default false.
  * @param useWindowAsScrollContainer If you want, you can set the window as the
  *                                   scrolling container. Default false.
  * @param hideSortableGhost Whether to auto-hide the ghost element. By default, as a convenience,
  *                          React Sortable List will automatically hide the element that is currently
  *                          being sorted. Set this to false if you would like to apply your own styling.
  *                          Default true.
  * @param lockToContainerEdges You can lock movement of the sortable element to it's parent SortableContainer.
  *                             Default false.
  * @param onSortEnd Callback that gets invoked when sorting ends.
  */
case class SortableContainer(
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
  onSortEnd: Permutation => Callback = p => Callback{}
  //onSortStart <- undef or function({node, index, collection}, event)
  //onSortMove <- undef or function(event)
) {

  import SortableContainer._

  def toJS = {
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

    def permutationFromJS(p: PermutationJS): Permutation = Permutation(p.oldIndex, p.newIndex)

    val onSortEndJS: (PermutationJS) => Unit = pjs => {
      val p = permutationFromJS(pjs)
      onSortEnd(p).runNow()
    }

    p.updateDynamic("onSortEnd")(onSortEndJS)

    //TODO other callbacks

    p
  }

  def apply(wrappedComponent: ReactComponentC[_,_,_,_]) = {
    //SortableContainer is a HOC, so we pass it a wrapped component to get back a SortableElement component
    val componentFactory = js.Dynamic.global.SortableContainer(wrappedComponent.factory)

    //Now we use that component to make a factory
    val component = React.asInstanceOf[js.Dynamic].createFactory(componentFactory)

    //Finally pass the props as JS to the factory to make an unmounted react component
    component(toJS).asInstanceOf[ReactComponentU_]
  }
}


