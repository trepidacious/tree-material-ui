package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui.{DeterminateIndeterminate, Mui, MuiCircularProgress}
import japgolly.scalajs.react.Key
import japgolly.scalajs.react.vdom.html_<^._
import org.rebeam.tree.DeltaIOContextSource
import org.rebeam.tree.demo.DemoRoutes.RefFailurePage
import org.rebeam.tree.demo.RefFailureData._
import org.rebeam.tree.ref.MirrorAndId
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.pages.{Pages, PagesView}

object RefFailureViews {

  implicit val contextSource = DeltaIOContextSource.default

  val RefEmptyView = PageLayout(
    MaterialColor.BlueGrey(500), 128,
    "Loading Ref Failure Demo...",
    None,
    Some(
      MuiCircularProgress(
        mode = DeterminateIndeterminate.indeterminate,
        color = Mui.Styles.colors.white
      )()
    ),
    None
  )

  val DataItemView = cursorView[DataItem, Pages[RefFailurePage.type, RefFailurePage.type]]("DataItemView"){
    c=> <.div(
      "DataItem " + c.model.id,
      textView(c.zoom(DataItem.name).label("Name"))
    )
  }

  val DataLinkView = cursorView[DataLink, Pages[RefFailurePage.type, RefFailurePage.type]]("DataLinkView"){
    c=> <.div(
        "DataLink " + c.model.id: VdomNode,
//        DataItemView(c.followRef(c.model.link))
        c.followRef(c.model.link).map(i => DataItemView(i)).whenDefined

    )
  }

  val RefView = cursorView[DataLinkPair, Pages[RefFailurePage.type, RefFailurePage.type]]("RefView"){
    c=> {

      val contents = <.div(
        "a": VdomNode,
        c.followRef(c.model.a).map(i => DataLinkView(i)).whenDefined,
        "b": VdomNode,
        c.followRef(c.model.b).map(i => DataLinkView(i)).whenDefined
      )

      PageLayout(
        color = MaterialColor.BlueGrey(500),
        toolbarText = "Ref Link Pair",
        listFAB = None,
        title = None,
        contents = Some(contents),
        scrollContents = true
      )
    }
  }

  val RefPagesView = PagesView[DataLinkPair, RefFailurePage.type]("RefPagesView"){
    cp =>
      List[(Key, VdomElement)](
        (0, RefView(cp): VdomElement)
      )
  }

  val RefMirrorView = cursorView[MirrorAndId[DataLinkPair], Pages[RefFailurePage.type, RefFailurePage.type]]("RefMirrorView"){
    c =>
      c.followRef(org.rebeam.tree.sync.Ref(c.model.id))
        .map(RefPagesView(_): VdomElement)
        .getOrElse(RefEmptyView: VdomElement)
  }

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val refViewFactory = ServerRootComponent.factory[MirrorAndId[DataLinkPair], Pages[RefFailurePage.type, RefFailurePage.type]](RefEmptyView, "api/reffailure") {
    RefMirrorView(_)
  }

//  val DataItemSummary = ListItem.listItemWithContentsAndDelete[DataItem](
//    "DataItemSummary",
//    cp => ListTextView(cp.zoom(DataItem.name).label("Name")),
////    item => avatarArcHashString(item.name)
//    item => avatarArcHashId(item.id)
//  )
//
//  val DataItemListView = ListView.usingRef[DataLinkPair, Pages[RefFailurePage.type, RefFailurePage.type], DataItem, DeleteAction](
//    "DataItemListView",
//    _.zoom(DataLinkPair.items),
//    (dataItem, dataItemListCursor) => DeleteAction(
//      dataItemListCursor.act(DataLinkPair.DeleteDataItemById(dataItem.id): DataLinkPair)
//    ),
//    DataItemSummary(_),
//    "Data items",
//    ListView.ListMode.Finite
//  )

}

