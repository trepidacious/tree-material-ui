package org.rebeam.tree.demo

import org.rebeam.tree._
import org.rebeam.lenses.macros.Lenses
import org.rebeam.tree.view.Color
import io.circe._
import io.circe.generic.semiauto._

import scala.language.higherKinds
import BasicDeltaDecoders._
import DeltaCodecs._
import io.circe.generic.JsonCodec
import org.rebeam.tree.sync.Sync.ModelIdGen

object DemoData {

  @JsonCodec
  case class IdOf[A](value: Int) extends AnyVal {
    def next: IdOf[A] = IdOf[A](value + 1)
    override def toString: String = s"#$value"
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
      def apply(s: Street): Street = s.copy(number = s.name.length * multiple)
    }

    case object Capitalise extends StreetAction {
      def apply(s: Street): Street = s.copy(name = s.name.toLowerCase.capitalize)
    }
  }

  // Alternative to @JsonCodec
//  implicit val streetDecoder: Decoder[Street] = deriveDecoder[Street]
//  implicit val streetEncoder: Encoder[Street] = deriveEncoder[Street]

  implicit val streetDeltaDecoder =
    value[Street] or lensN(Street.name) or lensN(Street.number) or lensN(Street.temperature) or action[Street, StreetAction]

  implicit val addressDeltaDecoder = value[Address] or lensN(Address.street)

  implicit val addressIdGen = new ModelIdGen[Address] {
    def genId(a: Address) = None
  }



  @JsonCodec
  sealed trait Priority
  object Priority {
    object Low extends Priority
    object Medium extends Priority
    object High extends Priority
  }

  @JsonCodec
  @Lenses
  case class Todo (
                            id: IdOf[Todo],
                            name: String,
                            created: Moment,
                            completed: Boolean = false,
                            priority: Priority = Priority.Medium
                          )

  @JsonCodec
  sealed trait TodoAction extends Delta[Todo]
  object TodoAction {
//    case class Complete(completed: Moment) extends TodoAction {
//      def apply(t: Todo): Todo = t.copy(completed = Some(completed))
//    }
    case object CyclePriority extends TodoAction {
      def apply(t: Todo): Todo = t.copy(priority =
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

  @JsonCodec
  @Lenses
  case class TodoList (
    id: IdOf[TodoList],
    name: String,
    created: Moment,
    priority: Priority = Priority.Medium,
    color: Color = Color.White,
    items: List[Todo] = Nil,
    nextTodoId: IdOf[Todo] = IdOf[Todo](1)
  )

  //Works with Cursor.zoomMatch to zoom to a particular Todo
  @JsonCodec
  case class FindTodoById(id: IdOf[Todo]) extends (Todo => Boolean) {
    def apply(t: Todo): Boolean = t.id == id
  }

  @JsonCodec
  sealed trait TodoListAction extends Delta[TodoList]
  object TodoListAction {

    case class CreateTodo(created: Moment, name: String = "New todo", priority: Priority = Priority.Medium) extends TodoListAction {
      def apply(l: TodoList): TodoList = {
        val t = Todo(l.nextTodoId, name, created, false, priority)
        l.copy(items = t :: l.items, nextTodoId = l.nextTodoId.next)
      }
    }

    case class DeleteExactTodo(t: Todo) extends TodoListAction {
      def apply(l: TodoList): TodoList = l.copy(items = l.items.filterNot(_ == t))
    }

    case class DeleteTodoById(id: IdOf[Todo]) extends TodoListAction {
      def apply(l: TodoList): TodoList = l.copy(items = l.items.filterNot(_.id == id))
    }
  }

  @JsonCodec
  case class TodoProjectId(value: Int) extends AnyVal {
    def next: TodoProjectId = TodoProjectId(value + 1)
  }
  object TodoProjectId {
    val first: TodoProjectId = TodoProjectId(1)
  }

  @JsonCodec
  @Lenses
  case class TodoProject (
                        id: TodoProjectId,
                        name: String,
                        lists: List[TodoList],
                        nextListId: IdOf[TodoList] = IdOf[TodoList](1)
                      )

  @JsonCodec
  sealed trait TodoProjectAction extends Delta[TodoProject]
  object TodoProjectAction {

    case class CreateTodoList(created: Moment, name: String = "New todo list", priority: Priority = Priority.Medium) extends TodoProjectAction {
      def apply(p: TodoProject): TodoProject = {
        val t = TodoList(p.nextListId, name, created, priority)
        p.copy(lists = t :: p.lists, nextListId = p.nextListId.next)
      }
    }

    case class DeleteExactList(l: TodoList) extends TodoProjectAction {
      def apply(p: TodoProject): TodoProject = p.copy(lists = p.lists.filterNot(_ == l))
    }

    case class DeleteListById(id: IdOf[TodoList]) extends TodoProjectAction {
      def apply(p: TodoProject): TodoProject = p.copy(lists = p.lists.filterNot(_.id == id))
    }
  }

  //Works with Cursor.zoomMatch to zoom to a particular TodoList
  @JsonCodec
  case class FindTodoListById(id: IdOf[TodoList]) extends (TodoList => Boolean) {
    def apply(t: TodoList): Boolean = t.id == id
  }


  //These don't have codecs in their own file
  implicit val colorDecoder: Decoder[Color] = deriveDecoder[Color]
  implicit val colorEncoder: Encoder[Color] = deriveEncoder[Color]
  implicit val momentDecoder: Decoder[Moment] = deriveDecoder[Moment]
  implicit val momentEncoder: Encoder[Moment] = deriveEncoder[Moment]

  //Delta decoders

  //These can only be replaced with a new value
  implicit val priorityDeltaDecoder = value[Priority]
  implicit val colorDeltaDecoder = value[Color]

  implicit val todoDeltaDecoder = value[Todo] or lensN(Todo.name) or lensN(Todo.priority) or lensN(Todo.completed) or action[Todo, TodoAction]

  //This makes it possible to act on any List[Todo] using an OptionalIDelta or an OptionalMatchDelta
  implicit val listOfTodoDeltaDecoder = optionalI[Todo] or optionalMatch[Todo, FindTodoById]

  implicit val todoListDeltaDecoder =
      value[TodoList] or lensN(TodoList.name) or lensN(TodoList.items) or lensN(TodoList.priority) or lensN(TodoList.color) or action[TodoList, TodoListAction]

  implicit val todoListIdGen = new ModelIdGen[TodoList] {
    def genId(a: TodoList) = None
  }

  //This makes it possible to act on any List[TodoList] using an OptionalIDelta or an OptionalMatchDelta
  implicit val listOfTodoListDeltaDecoder = optionalI[TodoList] or optionalMatch[TodoList, FindTodoListById]

  implicit val todoProjectDeltaDecoder =
    value[TodoProject] or lensN(TodoProject.name) or lensN(TodoProject.lists)

  implicit val todoProjectIdGen = new ModelIdGen[TodoProject] {
    def genId(a: TodoProject) = None
  }

}

