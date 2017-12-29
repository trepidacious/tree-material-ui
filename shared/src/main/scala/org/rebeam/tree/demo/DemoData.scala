package org.rebeam.tree.demo

import org.rebeam.tree._
import monocle.macros.Lenses
import org.rebeam.tree.view.{Color, MaterialColor}
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

import scala.language.higherKinds
import io.circe.generic.JsonCodec
import org.rebeam.tree.Delta._
import org.rebeam.tree.ValueDelta._
import org.rebeam.tree.ref.Mirror
import org.rebeam.tree.sync._
import org.rebeam.tree.sync.Sync._
import cats.instances.list._
import cats.syntax.traverse._
import io.circe.CursorOp.Find
import monocle.Lens
import org.rebeam.tree.Searchable.notSearchable

import scala.collection.immutable.Set
import scala.collection.mutable.ListBuffer

object DemoData {

  @JsonCodec
  @Lenses
  case class Street(name: String, number: Int, temperature: Double)

  @JsonCodec
  @Lenses
  case class Address(street: Street)

//  @JsonCodec
//  @Lenses
//  case class Company(address: Address)
//
//  @JsonCodec
//  @Lenses
//  case class Employee(name: String, company: Company)

  @JsonCodec
  sealed trait StreetDelta extends Delta[Nothing, Street]
  object StreetDelta {

    case class NumberMultiple(multiple: Int) extends StreetDelta {
      def apply(s: Street): DeltaIO[Nothing, Street] = pure(s.copy(number = s.name.length * multiple))
    }

    case object Capitalise extends StreetDelta {
      def apply(s: Street): DeltaIO[Nothing, Street] = pure(s.copy(name = s.name.toLowerCase.capitalize))
    }

    case class Value(s: Street) extends AbstractValueDelta[Street](s) with StreetDelta

    // Street, StreetDelta, String, StringValueDelta, Street.name, "name"
    // Street and String follow from Street.name
    // So need to provide StreetDelta, Street.name, StringValueDelta, "name"
    val name = DLens.apply[Nothing, Street, StreetDelta, Nothing, String, StringValueDelta](Street.name, Name)
    case class Name(d: StringValueDelta) extends LensDelta[Nothing, Street, Nothing, String](Street.name, d) with StreetDelta

    val number = DLens.apply[Nothing, Street, StreetDelta, Nothing, Int, IntValueDelta](Street.number, Number)
    case class Number(d: IntValueDelta) extends LensDelta[Nothing, Street, Nothing, Int](Street.number, d) with StreetDelta

    // Can we make do with just temperature value?
    val temperature = DLens.apply[Nothing, Street, StreetDelta, Nothing, Double, DoubleValueDelta](Street.temperature, Temperature)
    case class Temperature(d: DoubleValueDelta) extends LensDelta[Nothing, Street, Nothing, Double](Street.temperature, d) with StreetDelta
  }


  @JsonCodec
  sealed trait AddressDelta extends Delta[Nothing, Address]
  object AddressDelta {

    case class Value(a: Address) extends AbstractValueDelta[Address](a) with AddressDelta

    val street = DLens.apply[Nothing, Address, AddressDelta, Nothing, Street, StreetDelta](Address.street, StreetD)
    case class StreetD(d: StreetDelta) extends LensDelta[Nothing, Address, Nothing, Street](Address.street, d) with AddressDelta
  }

  implicit val addressIdGen = new ModelIdGen[Address] {
    def genId(a: Address) = None
  }

  implicit val addressRefAdder: RefAdder[Nothing, Address] = RefAdder.noOpRefAdder[Nothing, Address]


  //These don't have codecs in their own file
  implicit val colorDecoder: Decoder[Color] = deriveDecoder[Color]
  implicit val colorEncoder: Encoder[Color] = deriveEncoder[Color]
  implicit val momentDecoder: Decoder[Moment] = deriveDecoder[Moment]
  implicit val momentEncoder: Encoder[Moment] = deriveEncoder[Moment]

//  sealed trait Priority
//  object Priority {
//
//    object Low extends Priority {
//      override def toString = "low"
//    }
//    object Medium extends Priority {
//      override def toString = "medium"
//    }
//    object High extends Priority {
//      override def toString = "high"
//    }
//
//    implicit val s: Searchable[Priority, Guid] = notSearchable
//    implicit def notSearchable[Q]: Searchable[Priority, Q] = notSearchable
//
//    implicit val priorityEncoder: Encoder[Priority] = Encoder.instance {
//      case Low    => "Low".asJson
//      case Medium => "Medium".asJson
//      case High   => "High".asJson
//    }
//
//    implicit val priorityDecoder: Decoder[Priority] = Decoder[String].flatMap(
//      s => s match {
//        case "Low"    => Decoder.const(Low)
//        case "Medium" => Decoder.const(Medium)
//        case "High"   => Decoder.const(High)
//        case _ => Decoder.failedWithMessage("Invalid priority string " + s)
//      }
//    )
//
//  }

  @JsonCodec
  case class Priority(i: Int)
  object Priority {
    val Low = Priority(1)
    val Medium = Priority(2)
    val High = Priority(3)
  }

  //These can only be replaced with a new value
  @JsonCodec
  case class PriorityValueDelta(p: Priority) extends AbstractValueDelta[Priority](p)

  @JsonCodec
  case class ColorValueDelta(c: Color) extends AbstractValueDelta[Color](c)

  sealed trait TodoData
  object TodoData {

    @JsonCodec
    @Lenses
    case class Todo (
      id: Id[Todo],
      name: String,
      completed: Boolean = false,
      priority: Priority = Priority.Medium
    ) extends TodoData with Identified[Todo]

    @JsonCodec
    @Lenses
    case class TodoList (
      id: Id[TodoList],
      name: String,
      priority: Priority = Priority.Medium,
      color: Color = MaterialColor.Grey(500),
      items: List[Todo] = Nil
    ) extends TodoData with Identified[TodoList]

    @JsonCodec
    @Lenses
    case class TodoProject (
       id: Id[TodoProject],
       name: String,
       color: Color = MaterialColor.Grey(500),
       lists: List[TodoList]
     ) extends TodoData with Identified[TodoProject]

    import Searchable._
    implicit val todoDataSearchable: Searchable[TodoData, Guid] = new Searchable[TodoData, Guid] {
      def find(p: Guid => Boolean)(a: TodoData): Set[Guid] = a match {
        case x@Todo(_, _, _, _)         => x.deepFind(p)
        case x@TodoList(_, _, _, _, _)  => x.deepFind(p)
        case x@TodoProject(_, _, _, _)  => x.deepFind(p)
      }
    }

    import org.rebeam.tree.util.CirceUtils._

    implicit val todoDataEncoder: Encoder[TodoData] = Encoder.instance {
      case x@Todo(_, _, _, _)         => enc("Todo", x)
      case x@TodoList(_, _, _, _, _)  => enc("TodoList", x)
      case x@TodoProject(_, _, _, _)  => enc("TodoProject", x)
    }

    implicit val todoDataDecoder: Decoder[TodoData] = dec[TodoData] {
      case "Todo" => Decoder[Todo]
      case "TodoList" => Decoder[TodoList]
      case "TodoProject" => Decoder[TodoProject]
    }

  }

  import TodoData._

  @JsonCodec
  sealed trait TodoDelta extends Delta[TodoData, Todo]
  object TodoDelta {
    case object CyclePriority extends TodoDelta {
      def apply(t: Todo): DeltaIO[TodoData, Todo] = pure {
        t.copy(priority =
          if (t.priority == Priority.Low) {
            Priority.Medium
          } else if (t.priority == Priority.Medium) {
            Priority.High
          } else {
            Priority.Low
          }
        )
      }
    }

    val name = DLens.apply[TodoData, Todo, TodoDelta, Nothing, String, StringValueDelta](Todo.name, NameDelta)
    case class NameDelta(d: StringValueDelta) extends LensDelta[TodoData, Todo, Nothing, String](Todo.name, d) with TodoDelta

    val priority = DLens.apply[TodoData, Todo, TodoDelta, Nothing, Priority, PriorityValueDelta](Todo.priority, PriorityDelta)
    case class PriorityDelta(d: PriorityValueDelta) extends LensDelta[TodoData, Todo, Nothing, Priority](Todo.priority, d) with TodoDelta

    val completed = DLens.apply[TodoData, Todo, TodoDelta, Nothing, Boolean, BooleanValueDelta](Todo.completed, CompletedDelta)
    case class CompletedDelta(d: BooleanValueDelta) extends LensDelta[TodoData, Todo, Nothing, Boolean](Todo.completed, d) with TodoDelta
  }

  @JsonCodec
  sealed trait TodoListDelta extends Delta[TodoData, TodoList]
  object TodoListDelta {

    case class CreateTodo(name: String = "New todo", priority: Priority = Priority.Medium) extends TodoListDelta {
      def apply(l: TodoList): DeltaIO[TodoData, TodoList] = for {
        id <- getId[TodoData, Todo]
      } yield {
        val t = Todo(id, name, completed = false, priority)
        l.copy(items = t :: l.items)
      }
    }

    case class DeleteExactTodo(t: Todo) extends TodoListDelta {
      def apply(l: TodoList): DeltaIO[TodoData, TodoList] = pure(l.copy(items = l.items.filterNot(_ == t)))
    }

    case class DeleteTodoById(id: Id[Todo]) extends TodoListDelta {
      def apply(l: TodoList): DeltaIO[TodoData, TodoList] = pure(l.copy(items = l.items.filterNot(_.id == id)))
    }

    case class TodoIndexChange(oldIndex: Int, newIndex: Int) extends TodoListDelta {
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
      def apply(p: TodoList): DeltaIO[TodoData, TodoList] = pure {
        p.copy(items = updatedList(p.items))
      }
    }

    case object Archive extends TodoListDelta {
      def apply(p: TodoList): DeltaIO[TodoData, TodoList] = pure {
        p.copy(items = p.items.filterNot(todo => todo.completed))
      }
    }

    // Note we could also supply a DLens that would use ListIndexDelta, with a different name and different Delta case class
    val items = DLens.apply[TodoData, TodoList, TodoListDelta, TodoData, List[Todo], ListMatchDelta[TodoData, TodoData, Todo, TodoDelta, FindById[Todo]]](TodoList.items, ItemsDelta)
    case class ItemsDelta(d: ListMatchDelta[TodoData, TodoData, Todo, TodoDelta, FindById[Todo]]) extends LensDelta[TodoData, TodoList, TodoData, List[Todo]](TodoList.items, d) with TodoListDelta

    val name = DLens.apply[TodoData, TodoList, TodoListDelta, Nothing, String, StringValueDelta](TodoList.name, NameDelta)
    case class NameDelta(d: StringValueDelta) extends LensDelta[TodoData, TodoList, Nothing, String](TodoList.name, d) with TodoListDelta

    val priority = DLens.apply[TodoData, TodoList, TodoListDelta, Nothing, Priority, PriorityValueDelta](TodoList.priority, PriorityDelta)
    case class PriorityDelta(d: PriorityValueDelta) extends LensDelta[TodoData, TodoList, Nothing, Priority](TodoList.priority, d) with TodoListDelta

    val color = DLens.apply[TodoData, TodoList, TodoListDelta, Nothing, Color, ColorValueDelta](TodoList.color, ColorDelta)
    case class ColorDelta(d: ColorValueDelta) extends LensDelta[TodoData, TodoList, Nothing, Color](TodoList.color, d) with TodoListDelta

  }

  @JsonCodec
  sealed trait TodoProjectDelta extends Delta[TodoData, TodoProject]
  object TodoProjectDelta {

    case class CreateTodoList(name: String = "New todo list", priority: Priority = Priority.Medium) extends TodoProjectDelta {
      def apply(p: TodoProject): DeltaIO[TodoData, TodoProject] = for {
        id <- getId[TodoData, TodoList]
      } yield {
        val t = TodoList(id, name, priority)
        p.copy(lists = t :: p.lists)
      }
    }

    case class DeleteExactList(l: TodoList) extends TodoProjectDelta {
      def apply(p: TodoProject): DeltaIO[TodoData, TodoProject] = pure(p.copy(lists = p.lists.filterNot(_ == l)))
    }

    case class DeleteListById(id: Id[TodoList]) extends TodoProjectDelta {
      def apply(p: TodoProject): DeltaIO[TodoData, TodoProject] = pure(p.copy(lists = p.lists.filterNot(_.id == id)))
    }

    case class ListIndexChange(oldIndex: Int, newIndex: Int) extends TodoProjectDelta {
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
      def apply(p: TodoProject): DeltaIO[TodoData, TodoProject] = pure {
        p.copy(lists = updatedList(p.lists))
      }
    }

    val lists = DLens.apply[TodoData, TodoProject, TodoProjectDelta, TodoData, List[TodoList], ListMatchDelta[TodoData, TodoData, TodoList, TodoListDelta, FindById[TodoList]]](TodoProject.lists, ListsDelta)
    case class ListsDelta(d: ListMatchDelta[TodoData, TodoData, TodoList, TodoListDelta, FindById[TodoList]]) extends LensDelta[TodoData, TodoProject, TodoData, List[TodoList]](TodoProject.lists, d) with TodoProjectDelta

    val name = DLens.apply[TodoData, TodoProject, TodoProjectDelta, Nothing, String, StringValueDelta](TodoProject.name, NameDelta)
    case class NameDelta(d: StringValueDelta) extends LensDelta[TodoData, TodoProject, Nothing, String](TodoProject.name, d) with TodoProjectDelta

    val color = DLens.apply[TodoData, TodoProject, TodoProjectDelta, Nothing, Color, ColorValueDelta](TodoProject.color, ColorDelta)
    case class ColorDelta(d: ColorValueDelta) extends LensDelta[TodoData, TodoProject, Nothing, Color](TodoProject.color, d) with TodoProjectDelta

  }

  implicit val todoProjectIdGen = new ModelIdGen[TodoProject] {
    def genId(a: TodoProject): Option[ModelId] = None
  }

  implicit val mirrorIdGen = new ModelIdGen[Mirror[TodoData]] {
    def genId(a: Mirror[TodoData]): Option[ModelId] = None
  }

  implicit val todoListRefAdder: RefAdder[TodoData, TodoList] = RefAdder.noOpRefAdder[TodoData, TodoList]
  implicit val todoProjectRefAdder: RefAdder[TodoData, TodoProject] = RefAdder.noOpRefAdder[TodoData, TodoProject]

  object TodoExample {

    val listCount = 2
    val itemCount = 5

    def todoIO(i: Int): DeltaIO[TodoData, Todo] = for {
      id <- getId[TodoData, Todo]
    } yield {
      Todo(
        id,
        "Todo " + i,
        priority = i % 3 match {
          case 0 => Priority.Low
          case 1 => Priority.Medium
          case _ => Priority.High
        }
      )
    }

    def todoListIO(listIndex: Int): DeltaIO[TodoData, TodoList] = for {
      id <- getId[TodoData, TodoList]
      todos <- (1 to itemCount).toList.traverse(todoIO(_))
    } yield {
      TodoList(
        id,
        s"Todo list $listIndex",
        Priority.Medium,
        MaterialColor.backgroundForIndex(id.guid.withinDeltaId.id.toInt - 1),
        todos
      )
    }

    val todoProjectIO: DeltaIO[TodoData, TodoProject] = for{
      id <- getId[TodoData, TodoProject]
      lists <- (1 to listCount).toList.traverse(todoListIO(_))
    } yield {
      TodoProject(
        id,
        "Todo project",
        MaterialColor.Indigo(),
        lists
      )
    }

  }

}

