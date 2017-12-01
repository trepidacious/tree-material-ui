package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui.{DeterminateIndeterminate, Mui, MuiCircularProgress}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.DeltaIOContextSource
import org.rebeam.tree.demo.DemoRoutes.RefPage
import org.rebeam.tree.demo.RefData._
import org.rebeam.tree.ref.{Mirror, MirrorAndId}
import org.rebeam.tree.sync.Sync.{ClientDeltaId, ClientId}
import org.rebeam.tree.sync._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import RefData._
import org.rebeam.tree.view.list.ListItem.{DeleteAction, EditAndDeleteActions}
import org.rebeam.tree.view.list.{ListItem, ListTextView, ListView}
import org.rebeam.tree.view.pages.Pages

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

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val refViewFactory = ServerRootComponent.factory[MirrorAndId[DataItemList], Pages[RefPage.type, RefPage.type]](RefEmptyView, "api/refs") {
    RefMirrorView(_)
  }

  val RefMirrorView = cursorView[MirrorAndId[DataItemList], Pages[RefPage.type, RefPage.type]]("RefMirrorView"){
    c =>
//      System.out.println(c.model)
      c.followRef(org.rebeam.tree.sync.Ref(c.model.id))
      .map(RefView(_))
      .getOrElse(RefEmptyView)
  }

  val DataItemSummary = ListItem.listItemWithContentsAndDelete[DataItem](
    "DataItemSummary",
    cp => ListTextView(cp.zoom(DataItem.name).label("Name")),
    item => avatarArcHash(item.id.guid.toString())
  )

  val DataItemListView = ListView.usingRef[DataItemList, Pages[RefPage.type, RefPage.type], DataItem, DeleteAction](
    "DataItemListView",
    _.zoom(DataItemList.items),
    (dataItem, dataItemListCursor) => DeleteAction(
      dataItemListCursor.act(DataItemListAction.DeleteDataItemById(dataItem.id): DataItemListAction)
    ),
    DataItemSummary,
    "Data items",
    ListView.ListMode.Finite
  )

  val RefView = cursorView[DataItemList, Pages[RefPage.type, RefPage.type]]("RefView"){
    c=> {

      val fab = PageLayout.addFAB(c.act(DataItemListAction.CreateDataItem(): DataItemListAction))

      val contents = DataItemListView(
        p => c.act(DataItemListAction.DataItemIndexChange(p.oldIndex, p.newIndex): DataItemListAction)
      )(c)

      PageLayout(
        color = MaterialColor.BlueGrey(500),
        height = 128,
        toolbarText = "Ref List",
        listFAB = Some(fab),
        title = None,
        contents = Some(contents)//,
//        scrollContents = true
      )
    }
  }

}

