package org.rebeam.tree.demo

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import org.http4s._
import org.http4s.dsl._
import org.http4s.server.ServerApp
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent._
import org.http4s.server.websocket._
import org.rebeam.tree.{DeltaIOContext, DeltaIOContextSource, Moment}
import org.rebeam.tree.server.{ServerStore, ServerStoreValueExchange}
import org.rebeam.tree.view.MaterialColor
import DemoData._
import TaskData._
import org.rebeam.tree.Delta._
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.sync.DeltaIORun._
import cats.instances.list._
import cats.syntax.traverse._
import org.rebeam.tree.ref.{Mirror, Ref}

object ServerDemoApp extends ServerApp {

  val address = new ServerStore(Address(Street("OLD STREET", 1, 22.3)))

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
      MaterialColor.backgroundForIndex(id.id.toInt - 1),
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

  private val todoProject = todoProjectIO.runWith(
    DeltaIOContext(Moment(0)),
    DeltaId(ClientId(0), ClientDeltaId(0))
  ).data

  val todoProjectMirrorIO: DeltaIO[Mirror] = for {
    revision <- getId[TodoProject]
  } yield {
    Mirror.empty.updated(todoProject.id, todoProject, revision)
  }

  private val todoProjectMirror = todoProjectMirrorIO.runWith(
    DeltaIOContext(Moment(0)),
    DeltaId(ClientId(0), ClientDeltaId(1))
  ).data

  private val todoProjectStore = new ServerStore(todoProject)

  private val todoProjectMirrorStore = new ServerStore(todoProjectMirror)

  private val todoListStore = new ServerStore(todoProject.lists.head)

  val taskIO: DeltaIO[Task] = for {
    user <- User.create("A", "User", "user", Email("a@user.com"))
    task <- Task.example(Ref(user.id))
  } yield task

  private val task = taskIO.runWith(
    DeltaIOContext(Moment(0)),
    DeltaId(ClientId(0), ClientDeltaId(0))
  ).data

  private val taskStore = new ServerStore(task)

  // TODO better way of doing this - start from 1 since we use 0 to generate example data
  private val nextClientId = new AtomicLong(1)

  private val contextSource = DeltaIOContextSource.default

  val apiService = HttpService {

    case GET -> Root / "hello" =>
      Ok("Hello world!")

    case GET -> Root / "pwd" =>
      Ok(System.getProperty("user.dir"))

    case GET -> Root / "todolist" =>
      WS(
        ServerStoreValueExchange(
          todoListStore,
          ClientId(nextClientId.getAndIncrement()),
          contextSource
        )
      )

    case GET -> Root / "todoproject" =>
      WS(
        ServerStoreValueExchange(
          todoProjectStore,
          ClientId(nextClientId.getAndIncrement()),
          contextSource
        )
      )

    case GET -> Root / "todoprojectmirror" =>
      WS(
        ServerStoreValueExchange(
          todoProjectMirrorStore,
          ClientId(nextClientId.getAndIncrement()),
          contextSource
        )
      )

    case GET -> Root / "task" =>
      WS(
        ServerStoreValueExchange(
          taskStore,
          ClientId(nextClientId.getAndIncrement()),
          contextSource
        )
      )

    case GET -> Root / "address" =>
      WS(
        ServerStoreValueExchange(
          address,
          ClientId(nextClientId.getAndIncrement()),
          contextSource
        )
      )

  }

  //This will serve from java resources, so work in a jar
  //We can also set cacheStartegy = staticcontent.MemoryCache() in the Config
//  val assets = resourceService(ResourceService.Config("", "/"))

  //This serves directly from development resources directory, so will update
  //when we change original resources files and refresh browser
  //Serve our assets relative to user directory - kind of messy
  val assets = fileService(FileService.Config(new File(System.getProperty("user.dir")).getParent + "/assets", "/assets"))
  val assetsService: HttpService = HttpService {
    case r @ GET -> _ => assets(r)
  }

  //This will serve from java resources, so work in a jar
  //We can also set cacheStartegy = staticcontent.MemoryCache() in the Config
  //val resources = resourceService(ResourceService.Config("", "/"))

  //This serves directly from development resources directory, so will update
  //when we change original resources files and refresh browser
  val resources = fileService(FileService.Config("src/main/resources", "/"))

  val resourcesService: HttpService = HttpService {
    case r @ GET -> _ if r.pathInfo.isEmpty => resourcesService(r.withPathInfo("index.html"))
    case r @ GET -> _ if r.pathInfo.endsWith("/") => resourcesService(r.withPathInfo(r.pathInfo + "index.html"))
    case r @ GET -> _ => resources(r)
  }

  // val apiCORS = CORS(apiService)

  def server(args: List[String]) =
    BlazeBuilder.bindHttp(8080, "0.0.0.0")
      .withWebSockets(true)
      .mountService(apiService, "/api")
      .mountService(resourcesService, "/")
      .mountService(assetsService, "/")     //Note that the "/assets" path is already built into the fileService
      // .mountService(apiCORS, "/api")
      .start

}
