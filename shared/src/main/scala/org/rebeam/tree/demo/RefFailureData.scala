package org.rebeam.tree.demo

import io.circe.generic.JsonCodec
import io.circe.{Decoder, Encoder}
import monocle.macros.Lenses
import org.rebeam.tree.BasicDeltaDecoders._
import org.rebeam.tree.Delta._
import org.rebeam.tree.DeltaCodecs._
import org.rebeam.tree.ref._
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.sync._
import org.rebeam.tree._

object RefFailureData {

  @JsonCodec
  @Lenses
  case class DataItem(id: Id[DataItem], name: String) extends Identified[DataItem]

  @JsonCodec
  @Lenses
  case class DataLink(id: Id[DataLink], link: Ref[DataItem]) extends Identified[DataLink]

  @JsonCodec
  @Lenses
  case class DataLinkPair(id: Id[DataLinkPair], a: Ref[DataLink], b: Ref[DataLink]) extends Identified[DataLinkPair]

  implicit lazy val dataItemDeltaCodec: DeltaCodec[DataItem] =
    lens("name", DataItem.name)

  implicit lazy val dataLinkDeltaCodec: DeltaCodec[DataLink] =
    lens("link", DataLink.link)

  implicit lazy val dataLinkPairDeltaCodec: DeltaCodec[DataLinkPair] =
    lens("a", DataLinkPair.a) or lens("a", DataLinkPair.a)


  // Mirror codec to allow data to be handled by Mirror
  implicit val dataItemMirrorCodec: MirrorCodec[DataItem] = MirrorCodec[DataItem]("DataItem")
  implicit val dataLinkMirrorCodec: MirrorCodec[DataLink] = MirrorCodec[DataLink]("DataLink")

  implicit val dataLinkPairMirrorCodec: MirrorCodec[DataLinkPair] = MirrorCodec[DataLinkPair]("DataLinkPair")

  // Encoder and decoder for a Mirror of DataItems and DataItemLists
  implicit val mirrorDecoder: Decoder[Mirror] = Mirror.decoder(dataItemMirrorCodec, dataLinkMirrorCodec, dataLinkPairMirrorCodec)
  implicit val mirrorEncoder: Encoder[Mirror] = Mirror.encoder

  // DeltaCodec for a Mirror of DataItem or DataItemList
  implicit val mirrorDeltaCodec: DeltaCodec[Mirror] = DeltaCodecs.mirror[DataItem] or DeltaCodecs.mirror[DataLink] or DeltaCodecs.mirror[DataLinkPair]

  object DataItem {
    def create(name: String): DeltaIO[DataItem] = putPure(DataItem(_, name))
  }

  val exampleDataMirrorIO: DeltaIO[MirrorAndId[DataLinkPair]] = {
    for {
      i <- putPure[DataItem](DataItem(_, "i"))
      a <- putPure[DataLink](DataLink(_, Ref(i.id)))
      b <- putPure[DataLink](DataLink(_, Ref(i.id)))
      pair <- putPure[DataLinkPair](DataLinkPair(_, Ref(a.id), Ref(b.id)))
    } yield MirrorAndId(Mirror.empty, pair.id)  // Start with an empty mirror, the delta will add the data. Id is for the list.
  }

  implicit val mirrorIdGen = new ModelIdGen[MirrorAndId[DataLinkPair]] {
    def genId(a: MirrorAndId[DataLinkPair]) = None
  }

}
