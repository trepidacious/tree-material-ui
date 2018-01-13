package org.rebeam.tree.demo

import org.rebeam.tree._
import monocle.macros.Lenses
import org.rebeam.tree.view.{Color, Colored, MaterialColor}
import io.circe._
import io.circe.generic.semiauto._

import BasicDeltaDecoders._
import DeltaCodecs._
import io.circe.generic.JsonCodec
import org.rebeam.tree.Delta._
import org.rebeam.tree.ref.{Mirror, MirrorCodec}
import org.rebeam.tree.sync._
import org.rebeam.tree.sync.Sync._
import cats.instances.list._
import cats.syntax.traverse._

import scala.collection.mutable.ListBuffer
import Searchable._

object DemoData {

  @JsonCodec
  sealed trait Priority
  object Priority {
    object Low extends Priority {
      override def toString = "low"
    }
    object Medium extends Priority {
      override def toString = "medium"
    }
    object High extends Priority {
      override def toString = "high"
    }
    implicit val s: Searchable[Priority, Guid] = notSearchable
  }

  @JsonCodec
  @Lenses
  case class Street(name: String, number: Int, temperature: Double)

  @JsonCodec
  @Lenses
  case class Address(street: Street)

  @JsonCodec
  @Lenses
  case class Company(address: Address)

  @JsonCodec
  @Lenses
  case class Employee(name: String, company: Company)

  @JsonCodec
  sealed trait StreetAction extends Delta[Street]
  object StreetAction {
    case class NumberMultiple(multiple: Int) extends StreetAction {
      def apply(s: Street): DeltaIO[Street] = pure(s.copy(number = s.name.length * multiple))
    }

    case object Capitalise extends StreetAction {
      def apply(s: Street): DeltaIO[Street] = pure(s.copy(name = s.name.toLowerCase.capitalize))
    }
  }

  // Alternative to @JsonCodec
  //  implicit val streetDecoder: Decoder[Street] = deriveDecoder[Street]
  //  implicit val streetEncoder: Encoder[Street] = deriveEncoder[Street]

  implicit val streetDeltaDecoder: DeltaCodec[Street] =
    value[Street] or
      lens("street", Street.name) or
      lens("number", Street.number) or
      lens("temperature", Street.temperature) or
      action[Street, StreetAction]("StreetAction")

  implicit val addressDeltaDecoder: DeltaCodec[Address] =
    value[Address] or
      lens("street", Address.street)

  implicit val addressIdGen: ModelIdGen[Address] = _ => None

  @JsonCodec
  @Lenses
  case class Todo (
    id: Id[Todo],
    name: String,
    completed: Boolean = false,
    priority: Priority = Priority.Medium
  ) extends Identified[Todo]

  @JsonCodec
  sealed trait TodoAction extends Delta[Todo]
  object TodoAction {
    import Priority._
    case object CyclePriority extends TodoAction {
      def apply(t: Todo): DeltaIO[Todo] = pure {
        t.copy(priority = t.priority match {
          case Low => Medium
          case Medium => High
          case High => Low
        })
      }
    }
  }

  @JsonCodec
  @Lenses
  case class TodoList (
    id: Id[TodoList],
    name: String,
    priority: Priority = Priority.Medium,
    color: Color = MaterialColor.Grey(500),
    items: List[Todo] = Nil
  ) extends Identified[TodoList] with Colored

  //Works with Cursor.zoomMatch to zoom to a particular Todo
  @JsonCodec
  case class FindTodoById(id: Id[Todo]) extends (Todo => Boolean) {
    def apply(t: Todo): Boolean = t.id == id
  }

  @JsonCodec
  sealed trait TodoListAction extends Delta[TodoList]
  object TodoListAction {

    case class CreateTodo(name: String = "New todo", priority: Priority = Priority.Medium) extends TodoListAction {
      def apply(l: TodoList): DeltaIO[TodoList] = for {
        id <- getId[Todo]
      } yield {
        val t = Todo(id, name, completed = false, priority)
        l.copy(items = t :: l.items)
      }
    }

    case class DeleteExactTodo(t: Todo) extends TodoListAction {
      def apply(l: TodoList): DeltaIO[TodoList] = pure(l.copy(items = l.items.filterNot(_ == t)))
    }

    case class DeleteTodoById(id: Id[Todo]) extends TodoListAction {
      def apply(l: TodoList): DeltaIO[TodoList] = pure(l.copy(items = l.items.filterNot(_.id == id)))
    }

    case class TodoIndexChange(oldIndex: Int, newIndex: Int) extends TodoListAction {
      def updatedList[A](l: List[A]) = {
        if (oldIndex < 0 || oldIndex >= l.size || newIndex < 0 || newIndex >= l.size) {
          l
        } else {
          val lb = ListBuffer(l: _*)
          val e = lb.remove(oldIndex)
          lb.insert(newIndex, e)
          lb.toList
        }
      }
      def apply(p: TodoList): DeltaIO[TodoList] = pure {
        p.copy(items = updatedList(p.items))
      }
    }

    case object Archive extends TodoListAction {
      def apply(p: TodoList): DeltaIO[TodoList] = pure {
        p.copy(items = p.items.filterNot(todo => todo.completed))
      }
    }

  }

  @JsonCodec
  @Lenses
  case class TodoProject (
                        id: Id[TodoProject],
                        name: String,
                        color: Color = MaterialColor.Grey(500),
                        lists: List[TodoList]
                      ) extends Identified[TodoProject] with Colored

  @JsonCodec
  sealed trait TodoProjectAction extends Delta[TodoProject]
  object TodoProjectAction {

    case class CreateTodoList(name: String = "New todo list", priority: Priority = Priority.Medium) extends TodoProjectAction {
      def apply(p: TodoProject): DeltaIO[TodoProject] = for {
        id <- getId[TodoList]
      } yield {
        val t = TodoList(id, name, priority)
        p.copy(lists = t :: p.lists)
      }
    }

    case class DeleteExactList(l: TodoList) extends TodoProjectAction {
      def apply(p: TodoProject): DeltaIO[TodoProject] = pure(p.copy(lists = p.lists.filterNot(_ == l)))
    }

    case class DeleteListById(id: Id[TodoList]) extends TodoProjectAction {
      def apply(p: TodoProject): DeltaIO[TodoProject] = pure(p.copy(lists = p.lists.filterNot(_.id == id)))
    }

    case class ListIndexChange(oldIndex: Int, newIndex: Int) extends TodoProjectAction {
      def updatedList[A](l: List[A]) = {
        if (oldIndex < 0 || oldIndex >= l.size || newIndex < 0 || newIndex >= l.size) {
          l
        } else {
          val lb = ListBuffer(l: _*)
          val e = lb.remove(oldIndex)
          lb.insert(newIndex, e)
          lb.toList
        }
      }
      def apply(p: TodoProject): DeltaIO[TodoProject] = pure {
        p.copy(lists = updatedList(p.lists))
      }
    }

  }

  //Works with Cursor.zoomMatch to zoom to a particular TodoList
  @JsonCodec
  case class FindTodoListById(id: Id[TodoList]) extends (TodoList => Boolean) {
    def apply(t: TodoList): Boolean = t.id == id
  }


  //These don't have codecs in their own file
  implicit val colorDecoder: Decoder[Color] = deriveDecoder[Color]
  implicit val colorEncoder: Encoder[Color] = deriveEncoder[Color]
  implicit val momentDecoder: Decoder[Moment] = deriveDecoder[Moment]
  implicit val momentEncoder: Encoder[Moment] = deriveEncoder[Moment]

  //Delta decoders

  //These can only be replaced with a new value
  implicit val priorityDeltaDecoder: DeltaCodec[Priority] =
    value[Priority]

  implicit val colorDeltaDecoder: DeltaCodec[Color] =
    value[Color]

  implicit val todoDeltaDecoder: DeltaCodec[Todo] =
    value[Todo] or
      lens("name", Todo.name) or
      lens("priority", Todo.priority) or
      lens("completed", Todo.completed) or
      action[Todo, TodoAction]("TodoAction")

  //FIXME stop editing list as a value, implement an action to do permutations for react-sortable-hoc drags
  //This makes it possible to act on any List[Todo] using an OptionalIDelta or an OptionalMatchDelta
  implicit val listOfTodoDeltaDecoder: DeltaCodec[List[Todo]] =
    value[List[Todo]] or
      optionalI[Todo] or
      optionalMatch[Todo, FindTodoById]("FindTodoById") or
      optionalMatch[Todo, FindById[Todo]]("FindById")

  implicit val todoListDeltaDecoder: DeltaCodec[TodoList] =
      value[TodoList] or
        lens("name", TodoList.name) or
        lens("items", TodoList.items) or
        lens("priority", TodoList.priority) or
        lens("color", TodoList.color) or
        action[TodoList, TodoListAction]("TodoListAction")

  implicit val todoListIdGen: ModelIdGen[TodoList] = _ => None

  //FIXME stop editing list as a value, implement an action to do permutations for react-sortable-hoc drags
  //This makes it possible to act on any List[TodoList] using a complete new list, an OptionalIDelta or an OptionalMatchDelta
  implicit val listOfTodoListDeltaDecoder: DeltaCodec[List[TodoList]] =
    value[List[TodoList]] or
      optionalI[TodoList] or
      optionalMatch[TodoList, FindTodoListById]("FindTodoListById") or
      optionalMatch[TodoList, FindById[TodoList]]("FindById")

  implicit val todoProjectDeltaDecoder: DeltaCodec[TodoProject] =
    value[TodoProject] or
      lens("color", TodoProject.color) or
      lens("name", TodoProject.name) or
      lens("lists", TodoProject.lists) or
      action[TodoProject, TodoProjectAction]("TodoProjectAction")

  implicit val todoProjectIdGen: ModelIdGen[TodoProject] = _ => None

  implicit val mirrorIdGen: ModelIdGen[Mirror] = _ => None

  // Mirror codec to allow TodoProject to be handled by Mirror
  implicit val todoProjectMirrorCodec: MirrorCodec[TodoProject] =
    MirrorCodec[TodoProject]("todoProject")

  // Encoder, decoder and delta codec for entire Mirror
  implicit val mirrorDecoder: Decoder[Mirror] = Mirror.decoder(todoProjectMirrorCodec)
  implicit val mirrorEncoder: Encoder[Mirror] = Mirror.encoder
  implicit val mirrorDeltaCodec: DeltaCodec[Mirror] = DeltaCodecs.mirror[TodoProject]

  implicit val addressRefAdder: RefAdder[Address] = RefAdder.noOpRefAdder[Address]
  implicit val todoListRefAdder: RefAdder[TodoList] = RefAdder.noOpRefAdder[TodoList]
  implicit val todoProjectRefAdder: RefAdder[TodoProject] = RefAdder.noOpRefAdder[TodoProject]

  object TodoExample {

    val listCount = 2
    val itemCount = 5

    def todoIO(i: Int): DeltaIO[Todo] = for {
      id <- getId[Todo]
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

    def todoListIO(listIndex: Int): DeltaIO[TodoList] = for {
      id <- getId[TodoList]
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

    val todoProjectIO: DeltaIO[TodoProject] = for{
      id <- getId[TodoProject]
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

