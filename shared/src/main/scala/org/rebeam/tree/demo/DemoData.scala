package org.rebeam.tree.demo

import org.rebeam.tree._
import monocle.macros.Lenses
import org.rebeam.tree.view.{Color, MaterialColor}
import io.circe._
import io.circe.generic.semiauto._

import scala.language.higherKinds
import io.circe.generic.JsonCodec
import org.rebeam.tree.Delta._
import org.rebeam.tree.ValueDelta._
import org.rebeam.tree.ref.Mirror
import org.rebeam.tree.sync._
import org.rebeam.tree.sync.Sync._
import cats.instances.list._
import cats.syntax.traverse._
import monocle.Lens
import org.rebeam.tree.Searchable.notSearchable

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

    val name = DLens.apply[Nothing, Street, StreetDelta, Nothing, String, StringValueDelta](Street.name, Name)
    case class Name(d: StringValueDelta) extends LensDelta[Nothing, Street, String](Street.name, d) with StreetDelta

    val number = DLens.apply[Nothing, Street, StreetDelta, Nothing, Int, IntValueDelta](Street.number, Number)
    case class Number(d: IntValueDelta) extends LensDelta[Nothing, Street, Int](Street.number, d) with StreetDelta

    // Can we make do with just temperature value?
    val temperature = DLens.apply[Nothing, Street, StreetDelta, Nothing, Double, DoubleValueDelta](Street.temperature, Temperature)
    case class Temperature(d: DoubleValueDelta) extends LensDelta[Nothing, Street, Double](Street.temperature, d) with StreetDelta
  }


  @JsonCodec
  sealed trait AddressDelta extends Delta[Nothing, Address]
  object AddressDelta {

    case class Value(a: Address) extends AbstractValueDelta[Address](a) with AddressDelta

    val street = DLens.apply[Nothing, Address, AddressDelta, Nothing, Street, StreetDelta](Address.street, StreetD)
    case class StreetD(d: StreetDelta) extends LensDelta[Nothing, Address, Street](Address.street, d) with AddressDelta
  }

  implicit val addressIdGen = new ModelIdGen[Address] {
    def genId(a: Address) = None
  }

  implicit val addressRefAdder: RefAdder[Nothing, Address] = RefAdder.noOpRefAdder[Nothing, Address]

//  @JsonCodec
//  sealed trait Priority
//  object Priority {
//    object Low extends Priority {
//      override def toString = "low"
//    }
//    object Medium extends Priority {
//      override def toString = "medium"
//    }
//    object High extends Priority {
//      override def toString = "high"
//    }
//    implicit val s: Searchable[Priority, Guid] = notSearchable
//  }
//
//
//  @JsonCodec
//  @Lenses
//  case class Todo (
//                            id: Id[Todo],
//                            name: String,
//                            completed: Boolean = false,
//                            priority: Priority = Priority.Medium
//                          ) extends Identified[Todo]
//
//  @JsonCodec
//  sealed trait TodoAction extends Delta[Todo]
//  object TodoAction {
//    case object CyclePriority extends TodoAction {
//      def apply(t: Todo): DeltaIO[Todo] = pure {
//        t.copy(priority =
//          if (t.priority == Priority.Low) {
//            Priority.Medium
//          } else if (t.priority == Priority.Medium) {
//            Priority.High
//          } else {
//            Priority.Low
//          }
//        )
//      }
//    }
//  }
//
//  @JsonCodec
//  @Lenses
//  case class TodoList (
//    id: Id[TodoList],
//    name: String,
//    priority: Priority = Priority.Medium,
//    color: Color = MaterialColor.Grey(500),
//    items: List[Todo] = Nil
//  )
//
//  //Works with Cursor.zoomMatch to zoom to a particular Todo
//  @JsonCodec
//  case class FindTodoById(id: Id[Todo]) extends (Todo => Boolean) {
//    def apply(t: Todo): Boolean = t.id == id
//  }
//
//  @JsonCodec
//  sealed trait TodoListAction extends Delta[TodoList]
//  object TodoListAction {
//
//    case class CreateTodo(name: String = "New todo", priority: Priority = Priority.Medium) extends TodoListAction {
//      def apply(l: TodoList): DeltaIO[TodoList] = for {
//        id <- getId[Todo]
//      } yield {
//        val t = Todo(id, name, completed = false, priority)
//        l.copy(items = t :: l.items)
//      }
//    }
//
//    case class DeleteExactTodo(t: Todo) extends TodoListAction {
//      def apply(l: TodoList): DeltaIO[TodoList] = pure(l.copy(items = l.items.filterNot(_ == t)))
//    }
//
//    case class DeleteTodoById(id: Id[Todo]) extends TodoListAction {
//      def apply(l: TodoList): DeltaIO[TodoList] = pure(l.copy(items = l.items.filterNot(_.id == id)))
//    }
//
//    case class TodoIndexChange(oldIndex: Int, newIndex: Int) extends TodoListAction {
//      def updatedList[A](l: List[A]) = {
//        if (oldIndex < 0 || oldIndex >= l.size || newIndex < 0 || newIndex >= l.size) {
//          l
//        } else {
//          val lb = ListBuffer(l: _*)
//          val e = lb.remove(oldIndex)
//          lb.insert(newIndex, e)
//          lb.toList
//        }
//      }
//      def apply(p: TodoList): DeltaIO[TodoList] = pure {
//        p.copy(items = updatedList(p.items))
//      }
//    }
//
//    case object Archive extends TodoListAction {
//      def apply(p: TodoList): DeltaIO[TodoList] = pure {
//        p.copy(items = p.items.filterNot(todo => todo.completed))
//      }
//    }
//
//  }
//
//  @JsonCodec
//  @Lenses
//  case class TodoProject (
//                        id: Id[TodoProject],
//                        name: String,
//                        color: Color = MaterialColor.Grey(500),
//                        lists: List[TodoList]
//                      )
//
//  @JsonCodec
//  sealed trait TodoProjectAction extends Delta[TodoProject]
//  object TodoProjectAction {
//
//    case class CreateTodoList(name: String = "New todo list", priority: Priority = Priority.Medium) extends TodoProjectAction {
//      def apply(p: TodoProject): DeltaIO[TodoProject] = for {
//        id <- getId[TodoList]
//      } yield {
//        val t = TodoList(id, name, priority)
//        p.copy(lists = t :: p.lists)
//      }
//    }
//
//    case class DeleteExactList(l: TodoList) extends TodoProjectAction {
//      def apply(p: TodoProject): DeltaIO[TodoProject] = pure(p.copy(lists = p.lists.filterNot(_ == l)))
//    }
//
//    case class DeleteListById(id: Id[TodoList]) extends TodoProjectAction {
//      def apply(p: TodoProject): DeltaIO[TodoProject] = pure(p.copy(lists = p.lists.filterNot(_.id == id)))
//    }
//
//    case class ListIndexChange(oldIndex: Int, newIndex: Int) extends TodoProjectAction {
//      def updatedList[A](l: List[A]) = {
//        if (oldIndex < 0 || oldIndex >= l.size || newIndex < 0 || newIndex >= l.size) {
//          l
//        } else {
//          val lb = ListBuffer(l: _*)
//          val e = lb.remove(oldIndex)
//          lb.insert(newIndex, e)
//          lb.toList
//        }
//      }
//      def apply(p: TodoProject): DeltaIO[TodoProject] = pure {
//        p.copy(lists = updatedList(p.lists))
//      }
//    }
//
//  }
//
//  //Works with Cursor.zoomMatch to zoom to a particular TodoList
//  @JsonCodec
//  case class FindTodoListById(id: Id[TodoList]) extends (TodoList => Boolean) {
//    def apply(t: TodoList): Boolean = t.id == id
//  }
//
//
//  //These don't have codecs in their own file
//  implicit val colorDecoder: Decoder[Color] = deriveDecoder[Color]
//  implicit val colorEncoder: Encoder[Color] = deriveEncoder[Color]
//  implicit val momentDecoder: Decoder[Moment] = deriveDecoder[Moment]
//  implicit val momentEncoder: Encoder[Moment] = deriveEncoder[Moment]
//
//  //Delta decoders
//
//  //These can only be replaced with a new value
//  implicit val priorityDeltaDecoder = value[Priority]
//  implicit val colorDeltaDecoder = value[Color]
//
//  implicit val todoDeltaDecoder = value[Todo] or lensN(Todo.name) or lensN(Todo.priority) or lensN(Todo.completed) or action[Todo, TodoAction]
//
//  //FIXME stop editing list as a value, implement an action to do permutations for react-sortable-hoc drags
//  //This makes it possible to act on any List[Todo] using an OptionalIDelta or an OptionalMatchDelta
//  implicit val listOfTodoDeltaDecoder = value[List[Todo]] or optionalI[Todo] or optionalMatch[Todo, FindTodoById]
//
//  implicit val todoListDeltaDecoder =
//      value[TodoList] or lensN(TodoList.name) or lensN(TodoList.items) or lensN(TodoList.priority) or lensN(TodoList.color) or action[TodoList, TodoListAction]
//
//  implicit val todoListIdGen = new ModelIdGen[TodoList] {
//    def genId(a: TodoList) = None
//  }
//
//  //FIXME stop editing list as a value, implement an action to do permutations for react-sortable-hoc drags
//  //This makes it possible to act on any List[TodoList] using a complete new list, an OptionalIDelta or an OptionalMatchDelta
//  implicit val listOfTodoListDeltaDecoder = value[List[TodoList]] or optionalI[TodoList] or optionalMatch[TodoList, FindTodoListById]
//
//
//  implicit val todoProjectDeltaDecoder =
//    value[TodoProject] or lensN(TodoProject.color) or lensN(TodoProject.name) or lensN(TodoProject.lists) or action[TodoProject, TodoProjectAction]
//
//  implicit val todoProjectIdGen = new ModelIdGen[TodoProject] {
//    def genId(a: TodoProject) = None
//  }
//
//  implicit val mirrorIdGen = new ModelIdGen[Mirror] {
//    def genId(a: Mirror) = None
//  }
//
//  // Mirror codec to allow TodoProject to be handled by Mirror
//  implicit val todoProjectMirrorCodec: MirrorCodec[TodoProject] = MirrorCodec[TodoProject]("todoProject")
//
//  // Encoder, decoder and delta codec for entire Mirror
//  implicit val mirrorDecoder: Decoder[Mirror] = Mirror.decoder(todoProjectMirrorCodec)
//  implicit val mirrorEncoder: Encoder[Mirror] = Mirror.encoder
//  implicit val mirrorDeltaCodec: DeltaCodec[Mirror] = DeltaCodecs.mirror[TodoProject]
//
//  implicit val todoListRefAdder: RefAdder[TodoList] = RefAdder.noOpRefAdder[TodoList]
//  implicit val todoProjectRefAdder: RefAdder[TodoProject] = RefAdder.noOpRefAdder[TodoProject]
//
//  object TodoExample {
//
//    val listCount = 2
//    val itemCount = 5
//
//    def todoIO(i: Int): DeltaIO[Todo] = for {
//      id <- getId[Todo]
//    } yield {
//      Todo(
//        id,
//        "Todo " + i,
//        priority = i % 3 match {
//          case 0 => Priority.Low
//          case 1 => Priority.Medium
//          case _ => Priority.High
//        }
//      )
//    }
//
//    def todoListIO(listIndex: Int): DeltaIO[TodoList] = for {
//      id <- getId[TodoList]
//      todos <- (1 to itemCount).toList.traverse(todoIO(_))
//    } yield {
//      TodoList(
//        id,
//        s"Todo list $listIndex",
//        Priority.Medium,
//        MaterialColor.backgroundForIndex(id.guid.withinDeltaId.id.toInt - 1),
//        todos
//      )
//    }
//
//    val todoProjectIO: DeltaIO[TodoProject] = for{
//      id <- getId[TodoProject]
//      lists <- (1 to listCount).toList.traverse(todoListIO(_))
//    } yield {
//      TodoProject(
//        id,
//        "Todo project",
//        MaterialColor.Indigo(),
//        lists
//      )
//    }
//
//  }

}

