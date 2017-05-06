package org.rebeam.tree.demo

import io.circe._
import io.circe.generic.JsonCodec
import org.rebeam.lenses.macros.Lenses
import org.rebeam.tree.Delta._
import org.rebeam.tree.DeltaCodecs._
import org.rebeam.tree._
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.view.{Color, MaterialColor}

import scala.language.higherKinds

object TaskData {

  @JsonCodec
  sealed trait TaskPriority
  object TaskPriority {
    object Low extends TaskPriority {
      override def toString = "low"
    }
    object Medium extends TaskPriority {
      override def toString = "medium"
    }
    object High extends TaskPriority {
      override def toString = "high"
    }
  }

  @JsonCodec
  @Lenses
  case class TaskTag(name: String) extends AnyVal

  @JsonCodec
  @Lenses
  case class Email(email: String) extends AnyVal

  @JsonCodec
  @Lenses
  case class Markdown(contents: String) extends AnyVal

  @JsonCodec
  @Lenses
  case class User(id: Guid[User], firstName: String, lastName: String, userName: String, email: Email) extends HasId[User]

  object User {
    def create(firstName: String, lastName: String, userName: String, email: Email): DeltaIO[User] = for {
      id <- getId[User]
    } yield User(id, firstName, lastName, userName, email)
  }

  @JsonCodec
  sealed trait TaskState
  object TaskState {
    case object Active extends TaskState {
      override def toString = "active"
    }
    case object Inactive extends TaskState {
      override def toString = "inactive"
    }
    case object Complete extends TaskState {
      override def toString = "complete"
    }
  }

  @JsonCodec
  sealed trait TaskEvent extends Delta[Task] {
    def important: Boolean
  }
  object TaskEvent {
    case class Create(userId: Guid[User]) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.withEvent(this)
      val important = true
    }
    case class SetName(name: String) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(name = name).withEvent(this)
      val important = true
    }
    case class SetColor(color: Color) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(color = color).withEvent(this)
      val important = false
    }
    case class SetDescription(description: Markdown) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(description = description).withEvent(this)
      val important = true
    }
    case class SetPriority(priority: TaskPriority) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(priority = priority).withEvent(this)
      val important = true
    }
    case class SetState(state: TaskState) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(state = state).withEvent(this)
      val important = true
    }
    case class Tag(tag: TaskTag) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(tags = t.tags + tag).withEvent(this)
      val important = true
    }
    case class Untag(tag: TaskTag) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(tags = t.tags - tag).withEvent(this)
      val important = true
    }
    case class Watch(userId: Guid[User]) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(watching = t.watching + userId).withEvent(this)
      val important = true
    }
    case class Unwatch(userId: Guid[User]) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(watching = t.watching - userId).withEvent(this)
      val important = true
    }
    case class Lead(userId: Option[Guid[User]]) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(leading = userId).withEvent(this)
      val important = true
    }
    case class Comment(userId: Guid[User], markdown: Markdown) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.withEvent(this)
      val important = true
    }
  }

  @JsonCodec
  @Lenses
  case class Task(
                   id: Guid[Task],
                   name: String,
                   color: Color,
                   description: Markdown,
                   priority: TaskPriority,
                   state: TaskState,
                   tags: Set[TaskTag],
                   watching: Set[Guid[User]],
                   leading: Option[Guid[User]],
                   history: List[(Moment, TaskEvent)]
  ) {
    def withEvent(e: TaskEvent): DeltaIO[Task] = for {
      c <- getContext
    } yield {
      val entry = (c.moment, e)
      copy(history = history :+ entry)
    }
  }

  //Delta decoders
  implicit val taskDeltaDecoder: Decoder[Delta[Task]] = action[Task, TaskEvent]

  implicit val taskIdGen: ModelIdGen[Task] = new ModelIdGen[Task] {
    def genId(a: Task) = None
  }

  object Task {

    /**
      * Create a default task using given user id
      * @param userId User to create the task. Will also be set to watch the task.
      * @return A new task with default fields, having a single TaskEvent.created in history
      */
    def create(userId: Guid[User]): DeltaIO[Task] = for {
      id <- getId[Task]
      t = Task(
        id = id,
        name = "New task",
        color = MaterialColor.Blue(500),
        description = Markdown(""),
        priority = TaskPriority.Medium,
        state = TaskState.Active,
        tags = Set.empty,
        watching = Set(userId),
        leading = None,
        history = List.empty
      )
      tCreated <- TaskEvent.Create(userId).apply(t)
    } yield tCreated

    /**
      * Create an example task using given user id, with some interesting
      * data set showing different events
      * @param userId User to create the task. Will also be used to demonstrate leading/watching.
      * @return A new task with interesting data
      */
    def example(userId: Guid[User]): DeltaIO[Task] = {
      val events = List(
        TaskEvent.SetName("Example task"),
        TaskEvent.SetColor(MaterialColor.backgroundForIndex(0)),
        TaskEvent.SetDescription(Markdown(
          """
            | A task with some history to act as an example of features:
            | * SetName
            | * SetColor
            | * SetDescription
            | * Set priority low
            | * Set task inactive
            | * Add and remove some tags
            | * Set user to no longer watch task
            | * Assign user as leader, then remove leader
            | * Add some comments
          """.stripMargin)),
        TaskEvent.SetPriority(TaskPriority.Low),
        TaskEvent.SetState(TaskState.Inactive),
        TaskEvent.Tag(TaskTag("example")),
        TaskEvent.Tag(TaskTag("unwanted tag")),
        TaskEvent.Untag(TaskTag("unwanted tag")),
        TaskEvent.Unwatch(userId),
        TaskEvent.Lead(Some(userId)),
        TaskEvent.Lead(None),
        TaskEvent.Comment(userId, Markdown("Cool example!")),
        TaskEvent.Comment(userId, Markdown("YMMV!"))
      )

      // Start with a DeltaIO creating a default task, then within a DeltaIO,
      // apply each event to the task in sequence to produce the example task.
      events.foldLeft(
        Task.create(userId)
      ){
        case (taskIO, event) => taskIO.flatMap(task => event(task))
      }
    }
  }

}

