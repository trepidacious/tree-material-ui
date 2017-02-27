package org.rebeam.tree.demo

import org.rebeam.tree._
import org.rebeam.lenses.macros.Lenses
import org.rebeam.tree.view.{Color, MaterialColor}
import io.circe._
import io.circe.generic.semiauto._

import scala.language.higherKinds
import BasicDeltaDecoders._
import DeltaCodecs._
import io.circe.generic.JsonCodec
import org.rebeam.tree.Delta._
import org.rebeam.tree.sync.Sync._

import scala.collection.mutable.ListBuffer

object DemoData {

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

  implicit val streetDeltaDecoder =
    value[Street] or lensN(Street.name) or lensN(Street.number) or lensN(Street.temperature) or action[Street, StreetAction]

  implicit val addressDeltaDecoder = value[Address] or lensN(Address.street)

  implicit val addressIdGen = new ModelIdGen[Address] {
    def genId(a: Address) = None
  }

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
  }

  @JsonCodec
  @Lenses
  case class Todo (
                            id: Guid[Todo],
                            name: String,
                            completed: Boolean = false,
                            priority: Priority = Priority.Medium
                          )

  @JsonCodec
  sealed trait TodoAction extends Delta[Todo]
  object TodoAction {
    case object CyclePriority extends TodoAction {
      def apply(t: Todo): DeltaIO[Todo] = pure {
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
  }

  @JsonCodec
  @Lenses
  case class TodoList (
    id: Guid[TodoList],
    name: String,
    priority: Priority = Priority.Medium,
    color: Color = MaterialColor.Grey(500),
    items: List[Todo] = Nil
  )

  //Works with Cursor.zoomMatch to zoom to a particular Todo
  @JsonCodec
  case class FindTodoById(id: Guid[Todo]) extends (Todo => Boolean) {
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

    case class DeleteTodoById(id: Guid[Todo]) extends TodoListAction {
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
                        id: Guid[TodoProject],
                        name: String,
                        color: Color = MaterialColor.Grey(500),
                        lists: List[TodoList]
                      )

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

    case class DeleteListById(id: Guid[TodoList]) extends TodoProjectAction {
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
  case class FindTodoListById(id: Guid[TodoList]) extends (TodoList => Boolean) {
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

  //FIXME stop editing list as a value, implement an action to do permutations for react-sortable-hoc drags
  //This makes it possible to act on any List[Todo] using an OptionalIDelta or an OptionalMatchDelta
  implicit val listOfTodoDeltaDecoder = value[List[Todo]] or optionalI[Todo] or optionalMatch[Todo, FindTodoById]

  implicit val todoListDeltaDecoder =
      value[TodoList] or lensN(TodoList.name) or lensN(TodoList.items) or lensN(TodoList.priority) or lensN(TodoList.color) or action[TodoList, TodoListAction]

  implicit val todoListIdGen = new ModelIdGen[TodoList] {
    def genId(a: TodoList) = None
  }

  //FIXME stop editing list as a value, implement an action to do permutations for react-sortable-hoc drags
  //This makes it possible to act on any List[TodoList] using a complete new list, an OptionalIDelta or an OptionalMatchDelta
  implicit val listOfTodoListDeltaDecoder = value[List[TodoList]] or optionalI[TodoList] or optionalMatch[TodoList, FindTodoListById]


  implicit val todoProjectDeltaDecoder =
    value[TodoProject] or lensN(TodoProject.color) or lensN(TodoProject.name) or lensN(TodoProject.lists) or action[TodoProject, TodoProjectAction]

  implicit val todoProjectIdGen = new ModelIdGen[TodoProject] {
    def genId(a: TodoProject) = None
  }

}

