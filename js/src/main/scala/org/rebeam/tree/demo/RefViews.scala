package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui.{DeterminateIndeterminate, Mui, MuiCircularProgress}
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.DeltaIOContextSource
import org.rebeam.tree.demo.DemoRoutes.RefPage
import org.rebeam.tree.ref.MirrorAndId
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import RefData._
import japgolly.scalajs.react.Key
import org.rebeam.tree.view.list.ListItem.DeleteAction
import org.rebeam.tree.view.list.{ListItem, ListTextView, ListView}
import org.rebeam.tree.view.pages.{Pages, PagesView}

object RefViews {

  implicit val contextSource = DeltaIOContextSource.default

  val RefEmptyView = PageLayout(
    MaterialColor.BlueGrey(500), 128,
    "Loading Ref Demo...",
    None,
    Some(
      MuiCircularProgress(
        mode = DeterminateIndeterminate.indeterminate,
        color = Mui.Styles.colors.white
      )()
    ),
    None
  )

  val RefView = cursorView[DataItemList, Pages[RefPage.type, RefPage.type]]("RefView"){
    c=> {

      val fab = PageLayout.addFAB(c.act(DataItemListAction.CreateDataItem(): DataItemListAction))

      val contents = DataItemListView(
        p => c.act(DataItemListAction.DataItemIndexChange(p.oldIndex, p.newIndex): DataItemListAction)
      )(c)

      PageLayout(
        color = MaterialColor.BlueGrey(500),
        toolbarText = "Ref List",
        listFAB = Some(fab),
        title = None,
        contents = Some(contents),
        scrollContents = true
      )
    }
  }

  //TODO should we have a single page view that applies the styling needed for
  //PageLayout to work inside?
  val RefPagesView = PagesView[DataItemList, RefPage.type]("RefPagesView"){
    cp =>
      List[(Key, VdomElement)](
        (0, RefView(cp): VdomElement)
      )
  }

  val RefMirrorView = cursorView[MirrorAndId[DataItemList], Pages[RefPage.type, RefPage.type]]("RefMirrorView"){
    c =>
      c.followRef(org.rebeam.tree.sync.Ref(c.model.id))
        .map(RefPagesView(_): VdomElement)
        .getOrElse(RefEmptyView: VdomElement)
  }

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val refViewFactory = ServerRootComponent.factory[MirrorAndId[DataItemList], Pages[RefPage.type, RefPage.type]](RefEmptyView, "api/refs") {
    RefMirrorView(_)
  }

  val DataItemSummary = ListItem.listItemWithContentsAndDelete[DataItem](
    "DataItemSummary",
    cp => ListTextView(cp.zoom(DataItem.name).label("Name")),
//    item => avatarArcHashString(item.name)
    item => avatarArcHashId(item.id)
  )

  val DataItemListView = ListView.usingRef[DataItemList, Pages[RefPage.type, RefPage.type], DataItem, DeleteAction](
    "DataItemListView",
    _.zoom(DataItemList.items),
    (dataItem, dataItemListCursor) => DeleteAction(
      dataItemListCursor.act(DataItemListAction.DeleteDataItemById(dataItem.id): DataItemListAction)
    ),
    DataItemSummary(_),
    "Data items",
    ListView.ListMode.Finite
  )

}

