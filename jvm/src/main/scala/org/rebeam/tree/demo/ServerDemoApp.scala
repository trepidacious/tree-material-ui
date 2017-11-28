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
import org.rebeam.tree.Delta._
import org.rebeam.tree.sync.Sync._
import org.rebeam.tree.sync.DeltaIORun._
import org.rebeam.tree.demo.DemoData.Address
import org.rebeam.tree.demo.RefData.DataItemList
import org.rebeam.tree.ref.{Mirror, MirrorAndId}
import org.rebeam.tree.sync.{RefAdder, Ref}

object ServerDemoApp extends ServerApp {

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
      import DemoData._
      WS(
        ServerStoreValueExchange(
          todoProjectMirrorStore,
          ClientId(nextClientId.getAndIncrement()),
          contextSource
        )
      )

    case GET -> Root / "refs" =>
      import RefData._
      WS(
        ServerStoreValueExchange(
          refDemoStore,
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
