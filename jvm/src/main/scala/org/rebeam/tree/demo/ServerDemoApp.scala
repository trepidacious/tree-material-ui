package org.rebeam.tree.demo

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import cats.effect.{Effect, IO}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent._
import org.rebeam.tree.{DeltaIOContext, DeltaIOContextSource, Moment}
import org.rebeam.tree.server.{ServerStore, ServerStoreValueExchange}
import org.rebeam.tree.Delta._
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.sync.DeltaIORun._
import org.rebeam.tree.demo.DemoData.Address
import org.rebeam.tree.demo.RefData.DataItemList
import org.rebeam.tree.ref.{Mirror, MirrorAndId}
import org.rebeam.tree.sync.{Ref, RefAdder}

import scala.concurrent.ExecutionContext.Implicits.global

object ServerDemoAppIO extends ServerDemoApp[IO]

class ServerDemoApp[F[_]](implicit F: Effect[F]) extends StreamApp[F] with Http4sDsl[F] {

  val address: ServerStore[Address] = {
    import DemoData._
    new ServerStore(Address(Street("OLD STREET", 1, 22.3)))
  }

  private val todoProjectMirrorStore = {
    import DemoData._

    //FIXME update to use put
    val todoProjectMirrorIO: DeltaIO[Mirror] = for {
      todoProject <- TodoExample.todoProjectIO
      revision <- getGuid
    } yield {
      Mirror.empty.updated(todoProject.id, todoProject, revision)
    }

    val todoProjectMirror = todoProjectMirrorIO.runWith(
      DeltaIOContext(Moment(0)),
      DeltaId(ClientId(0), ClientDeltaId(0))
    ).data

    new ServerStore(todoProjectMirror)
  }

  private val todoListStore = {
    import DemoData._
    val todoProject = TodoExample.todoProjectIO.runWith(
      DeltaIOContext(Moment(0)),
      DeltaId(ClientId(0), ClientDeltaId(0))
    ).data
    new ServerStore(todoProject.lists.head)
  }

  private val todoProjectStore = {
    import DemoData._
    val todoProject = TodoExample.todoProjectIO.runWith(
      DeltaIOContext(Moment(0)),
      DeltaId(ClientId(0), ClientDeltaId(0))
    ).data
    new ServerStore(todoProject)
  }

  private val taskStore = {
    import TaskData._
    val taskIO: DeltaIO[Task] = for {
      user <- User.create("A", "User", "user", Email("a@user.com"))
      task <- Task.example(Ref(user.id))
    } yield task

    val task = taskIO.runWith(
      DeltaIOContext(Moment(0)),
      DeltaId(ClientId(0), ClientDeltaId(0))
    ).data
    new ServerStore(task)
  }

  private val refDemoStore: ServerStore[MirrorAndId[DataItemList]] = {
    import RefData._

    val result = RefData.exampleDataMirrorIO.runWith(
      DeltaIOContext(Moment(0)),
      DeltaId(ClientId(0), ClientDeltaId(0))
    )

    val mirrorAndId = RefAdder.mirrorAndIdRefAdder.addRefs(result)
    new ServerStore(mirrorAndId)
  }

  // TODO better way of doing this - start from 1 since we use 0 to generate example data
  // Can we make a stream and use this to produce incrementing values?
  private val nextClientId = new AtomicLong(1)

  private val contextSource = DeltaIOContextSource.default

  val apiService: HttpService[F] = HttpService[F] {

    case GET -> Root / "hello" =>
      Ok("Hello world!")

    case GET -> Root / "pwd" =>
      Ok(System.getProperty("user.dir"))

    case GET -> Root / "todolist" =>
      ServerStoreValueExchange(
        todoListStore,
        ClientId(nextClientId.getAndIncrement()),
        contextSource
      )

    case GET -> Root / "todoproject" =>
      ServerStoreValueExchange(
        todoProjectStore,
        ClientId(nextClientId.getAndIncrement()),
        contextSource
      )

    case GET -> Root / "todoprojectmirror" =>
      import DemoData._
      ServerStoreValueExchange(
        todoProjectMirrorStore,
        ClientId(nextClientId.getAndIncrement()),
        contextSource
      )

    case GET -> Root / "refs" =>
      import RefData._
      ServerStoreValueExchange(
        refDemoStore,
        ClientId(nextClientId.getAndIncrement()),
        contextSource
      )

    case GET -> Root / "task" =>
      ServerStoreValueExchange(
        taskStore,
        ClientId(nextClientId.getAndIncrement()),
        contextSource
      )

    case GET -> Root / "address" =>
      ServerStoreValueExchange(
        address,
        ClientId(nextClientId.getAndIncrement()),
        contextSource
      )

  }

  //This serves directly from development resources directory, so will update
  //when we change original resources files and refresh browser
  //Serve our assets relative to user directory - kind of messy
  val assets: HttpService[F] = fileService(FileService.Config(new File(System.getProperty("user.dir")).getParent + "/assets", "/assets"))

  //This serves directly from development resources directory, so will update
  //when we change original resources files and refresh browser
  val resources: HttpService[F] = fileService(FileService.Config("src/main/resources", "/"))

  val indexService: HttpService[F] = HttpService[F] {
    case request @ GET -> Root =>
      StaticFile.fromFile(new File("src/main/resources/index.html"), Some(request)).getOrElseF(NotFound())
  }

  // val apiCORS = CORS(apiService)

  def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    for {
      exitCode <- BlazeBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withWebSockets(true)
        .mountService(apiService, "/api")
        .mountService(indexService, "/")
        .mountService(resources, "/")
        .mountService(assets, "/")     //Note that the "/assets" path is already built into the fileService
        .serve
    } yield exitCode

}
