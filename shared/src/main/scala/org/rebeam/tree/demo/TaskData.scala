package org.rebeam.tree.demo

import io.circe.generic.JsonCodec
import monocle.macros.Lenses
import org.rebeam.tree.Delta._
import org.rebeam.tree.DeltaCodecs._
import org.rebeam.tree._
import org.rebeam.tree.sync._
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.view.{Color, MaterialColor}

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
  case class User(id: Id[User], firstName: String, lastName: String, userName: String, email: Email) extends Identified[User]

  object User {
    def create(firstName: String, lastName: String, userName: String, email: Email): DeltaIO[User] =
      //put(id => pure(User(id, firstName, lastName, userName, email)))
      for (
        id <- getId[User]
      ) yield User(id, firstName, lastName, userName, email)
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
    case class Create(userRef: Ref[User]) extends TaskEvent {
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
      def apply(t: Task): DeltaIO[Task] = t.copy(tags = tag :: t.tags.filterNot(_ == tag)).withEvent(this)
      val important = true
    }
    case class Untag(tag: TaskTag) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(tags = t.tags.filterNot(_ == tag)).withEvent(this)
      val important = true
    }
    case class Watch(userRef: Ref[User]) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(watching = userRef :: t.watching.filterNot(_.id == userRef.id)).withEvent(this)
      val important = true
    }
    case class Unwatch(userRef: Ref[User]) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(watching = t.watching.filterNot(_.id == userRef.id)).withEvent(this)
      val important = true
    }
    case class Lead(userRef: Option[Ref[User]]) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.copy(leading = userRef).withEvent(this)
      val important = true
    }
    case class Comment(userRef: Ref[User], markdown: Markdown) extends TaskEvent {
      def apply(t: Task): DeltaIO[Task] = t.withEvent(this)
      val important = true
    }
  }

  @JsonCodec
  @Lenses
  case class Task(
                   id: Id[Task],
                   name: String,
                   color: Color,
                   description: Markdown,
                   priority: TaskPriority,
                   state: TaskState,
                   tags: List[TaskTag],
                   watching: List[Ref[User]],
                   leading: Option[Ref[User]],
                   history: List[(Moment, TaskEvent)]
  ) {
    def withEvent(e: TaskEvent): DeltaIO[Task] = for {
      c <- getContext
    } yield {
      val entry = (c.moment, e)
      copy(history = history :+ entry)
    }
  }

  //Delta decoders. Note we provide lenses to watching and leading to reach the refs in them
  implicit lazy val taskDeltaDecoder: DeltaCodec[Task] =
    action[Task, TaskEvent]("TaskEvent") or
      lens("watching", Task.watching) or
      lens("leading", Task.leading)

  //TODO provide a means of making a DeltaCodec fail when used to actually decode a delta, but still perform ref updating
  //Get to the refs in watching and leading, so Mirror can update them.
  implicit lazy val optionRefUserDeltaDecoder: DeltaCodec[Option[Ref[User]]] = option[Ref[User]]
  implicit lazy val listRefUserDeltaDecoder: DeltaCodec[List[Ref[User]]] = optionalI[Ref[User]]

  implicit val taskIdGen: ModelIdGen[Task] = new ModelIdGen[Task] {
    def genId(a: Task) = None
  }

  object Task {

    /**
      * Create a default task using given user id
      * @param userRef User to create the task. Will also be set to watch the task.
      * @return A new task with default fields, having a single TaskEvent.created in history
      */
    def create(userRef: Ref[User]): DeltaIO[Task] = for {
      id <- getId[Task]
      t = Task(
        id = id,
        name = "New task",
        color = MaterialColor.Blue(500),
        description = Markdown(""),
        priority = TaskPriority.Medium,
        state = TaskState.Active,
        tags = Nil,
        watching = Nil,
        leading = None,
        history = List.empty
      )
      tCreated <- TaskEvent.Create(userRef).apply(t)
    } yield tCreated

    /**
      * Create an example task using given user id, with some interesting
      * data set showing different events
      * @param userRef User to create the task. Will also be used to demonstrate leading/watching.
      * @return A new task with interesting data
      */
    def example(userRef: Ref[User]): DeltaIO[Task] = {
      val events = List(
        TaskEvent.SetName("Example task"),
        TaskEvent.SetColor(MaterialColor.backgroundForIndex(0)),
        TaskEvent.SetDescription(Markdown(
          """
            | A task with some history to act as an example of features:
            | * Set name
            | * Set color
            | * Set description
            | * Set priority low
            | * Set task inactive
            | * Add and remove some tags
            | * Set user to no longer watch task
            | * Assign user as leader, then remove leader
            | * Add some comments
          """.stripMargin)),
        TaskEvent.SetPriority(TaskPriority.Low),
        TaskEvent.SetState(TaskState.Inactive),
        TaskEvent.Tag(TaskTag("example tag")),
        TaskEvent.Tag(TaskTag("unwanted tag")),
        TaskEvent.Untag(TaskTag("unwanted tag")),
        TaskEvent.Unwatch(userRef),
        TaskEvent.Lead(Some(userRef)),
        TaskEvent.Lead(None),
        TaskEvent.Comment(userRef, Markdown("Cool example!")),
        TaskEvent.Comment(userRef, Markdown("YMMV!"))
      )

      // Start with a DeltaIO creating a default task, then within a DeltaIO,
      // apply each event to the task in sequence to produce the example task.
      events.foldLeft(
        Task.create(userRef)
      ){
        case (taskIO, event) => taskIO.flatMap(task => event(task))
      }
    }
  }

  implicit val taskRefAdder: RefAdder[Task] = RefAdder.noOpRefAdder[Task]

}

