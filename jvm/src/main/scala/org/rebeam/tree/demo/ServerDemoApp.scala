package org.rebeam.tree.demo

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import org.http4s._
import org.http4s.dsl._
import org.http4s.server.ServerApp
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent._
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits._
import org.rebeam.tree.Moment
import org.rebeam.tree.server.{ServerStore, ServerStoreValueExchange, TreeStore, TreeStoreValueExchange}
import org.rebeam.tree.view.MaterialColor

import scala.concurrent.duration._
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.async.unboundedQueue
import scalaz.stream.time.awakeEvery
import scalaz.stream.{DefaultScheduler, Exchange, Process, Sink}
import DemoData._
import org.rebeam.tree.sync.Sync.{ClientId, ModelId, ModelIdGen}

object ServerDemoApp extends ServerApp {

//  val address = new TreeStore(Address(Street("OLD STREET", 1)))

  implicit val addressIdGen = new ModelIdGen[Address] {
    def genId(a: Address) = None
  }

  val address = new ServerStore(Address(Street("OLD STREET", 1, 22.3)))

  val todoList = {
    val time = System.currentTimeMillis()
    TodoList(
      "Todo", "trepidacious@gmail.com", MaterialColor.Amber(),
      (1 to 10).map(i => {
        Todo(
          i, "Item " + i, Moment(time - 60000 * (10 - i)),
          priority = i % 3 match {
            case 0 => Priority.Low
            case 1 => Priority.Medium
            case _ => Priority.High
          }
        )
      }).toList,
      11
    )
  }

  val todoListStore = new TreeStore(todoList)

  // TODO better way of doing this
  val nextClientId = new AtomicLong(0)

  val apiService = HttpService {

    case GET -> Root / "hello" =>
      Ok("Hello world!")

    case GET -> Root / "pwd" =>
      Ok(System.getProperty("user.dir"))

    case GET -> Root / "todolist" =>
      WS(TreeStoreValueExchange(todoListStore))

    case GET -> Root / "address" =>
      WS(ServerStoreValueExchange(address, ClientId(nextClientId.getAndIncrement())))

    case req@ GET -> Root / "ws" =>
      val src = awakeEvery(1.seconds)(Strategy.DefaultStrategy, DefaultScheduler).map{ d => Text(s"Ping! $d") }
      val sink: Sink[Task, WebSocketFrame] = Process.constant {
        case Text(t, _) => Task.delay(println(t))
        case f          => Task.delay(println(s"Unknown type: $f"))
      }
      WS(Exchange(src, sink))

    case req@ GET -> Root / "wsecho" =>
      val q = unboundedQueue[WebSocketFrame]
      val src = q.dequeue.collect {
        case Text(msg, _) => Text("Echoing: " + msg)
      }
      WS(Exchange(src, q.enqueue))

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
