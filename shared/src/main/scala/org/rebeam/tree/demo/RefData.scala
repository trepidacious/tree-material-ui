package org.rebeam.tree.demo

import io.circe.{Decoder, Encoder}
import io.circe.generic.JsonCodec
import org.rebeam.lenses.macros.Lenses
import org.rebeam.tree.Delta._
import org.rebeam.tree.DeltaCodecs
import org.rebeam.tree.DeltaCodecs._
import org.rebeam.tree.ref.{Mirror, MirrorCodec, Ref}
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.BasicDeltaDecoders._
import cats.instances.list._
import cats.instances.option._
import cats.syntax.traverse._

import scala.language.higherKinds

object RefData {

  @JsonCodec
  @Lenses
  case class DataItem(id: Guid[DataItem], name: String) extends HasId[DataItem]

  @JsonCodec
  @Lenses
  case class DataItemList(id: Guid[DataItemList], items: List[Ref[DataItem]]) extends HasId[DataItemList]

  implicit lazy val dataItemDeltaCodec: DeltaCodec[DataItem] = lensN(DataItem.name)

  implicit lazy val dataItemListDeltaCodec: DeltaCodec[DataItemList] = lensN(DataItemList.items)

  implicit lazy val listOfRefToDataItemDeltaDecoder: DeltaCodec[List[Ref[DataItem]]] = optionalI[Ref[DataItem]]

  // Mirror codec to allow DataItem and DataItemList to be handled by Mirror
  implicit val dataItemMirrorCodec: MirrorCodec[DataItem] = MirrorCodec[DataItem]("dataItem")
  implicit val dataItemListMirrorCodec: MirrorCodec[DataItemList] = MirrorCodec[DataItemList]("dataItemList")

  // Encoder and decoder for a Mirror of DataItems and DataItemLists
  implicit val mirrorDecoder: Decoder[Mirror] = Mirror.decoder(dataItemMirrorCodec, dataItemListMirrorCodec)
  implicit val mirrorEncoder: Encoder[Mirror] = Mirror.encoder

  // DeltaCodec for a Mirror of DataItem or DataItemList
  implicit val mirrorDeltaCodec: DeltaCodec[Mirror] = DeltaCodecs.mirror[DataItem] or DeltaCodecs.mirror[DataItemList]

  object DataItem {
    def create(name: String): DeltaIO[DataItem] = put(id => pure(DataItem(id, name)))
  }

  val exampleDataMirror: DeltaIO[DataItemList] = {
    for {
      items <- Range(1, 10).toList.traverse[DeltaIO, DataItem](
        i => putPure[DataItem](
          id => DataItem(id, "item " + i)
        )
      )
      list <- putPure[DataItemList](
        id => DataItemList(id, items.map(i => Ref(i.id)))
      )
    } yield list
  }

}
