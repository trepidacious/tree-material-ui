package org.rebeam.tree.demo

import io.circe.{Decoder, Encoder}
import io.circe.generic.JsonCodec
import org.rebeam.lenses.macros.Lenses
import org.rebeam.tree.Delta._
import org.rebeam.tree.{Delta, DeltaCodecs}
import org.rebeam.tree.DeltaCodecs._
import org.rebeam.tree.ref.{Mirror, MirrorCodec, Ref}
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.BasicDeltaDecoders._
import cats.instances.list._
import cats.instances.option._
import cats.syntax.traverse._

import scala.collection.mutable.ListBuffer
import scala.language.higherKinds

object RefData {

  @JsonCodec
  @Lenses
  case class DataItem(id: Guid[DataItem], name: String) extends HasId[DataItem]

  @JsonCodec
  @Lenses
  case class DataItemList(id: Guid[DataItemList], items: List[Ref[DataItem]]) extends HasId[DataItemList]


  @JsonCodec
  sealed trait DataItemListAction extends Delta[DataItemList]
  object DataItemListAction {

    case class CreateDataItem(name: String = "New todo") extends DataItemListAction {
      def apply(l: DataItemList): DeltaIO[DataItemList] = for {
        item <- putPure[DataItem](id => DataItem(id, "New Data Item"))
      } yield {
        l.copy(items = Ref(item.id) :: l.items)
      }
    }

    case class DeleteDataItemById(id: Guid[DataItem]) extends DataItemListAction {
      def apply(l: DataItemList): DeltaIO[DataItemList] = pure(l.copy(items = l.items.filterNot(_.id == id)))
    }

    case class DataItemIndexChange(oldIndex: Int, newIndex: Int) extends DataItemListAction {
      //FIXME refactor to utility
      private def updatedList[A](l: List[A]) = {
        if (oldIndex < 0 || oldIndex >= l.size || newIndex < 0 || newIndex >= l.size) {
          l
        } else {
          val lb = ListBuffer(l: _*)
          val e = lb.remove(oldIndex)
          lb.insert(newIndex, e)
          lb.toList
        }
      }
      def apply(p: DataItemList): DeltaIO[DataItemList] = pure {
        p.copy(items = updatedList(p.items))
      }
    }
  }

  implicit lazy val dataItemDeltaCodec: DeltaCodec[DataItem] = lensN(DataItem.name)

  implicit lazy val dataItemListDeltaCodec: DeltaCodec[DataItemList] = lensN(DataItemList.items) or action[DataItemList, DataItemListAction]

  //Make into optional match
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

  val exampleDataMirrorIO: DeltaIO[Mirror] = {
    for {
      items <- Range(1, 10).toList.traverse[DeltaIO, DataItem](
        i => putPure[DataItem](
          id => DataItem(id, "item " + i)
        )
      )
      list <- putPure[DataItemList](
        id => DataItemList(id, items.map(i => Ref(i.id)))
      )
    } yield Mirror.empty  // Start with an empty mirror, the delta will add the data
  }

  implicit val mirrorIdGen = new ModelIdGen[Mirror] {
    def genId(a: Mirror) = None
  }

}
