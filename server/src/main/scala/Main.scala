package crashbox.ci

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {
  def log(message: String) = System.err.println(message)

  log("App starts")

  log("System components initializing")
  implicit val system = ActorSystem("authentication")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = system.settings.config.getConfig("crashboxd")

  log("Database configuration initializing")
  val db = new dal.DatabaseConfig {
    override val profile = slick.jdbc.H2Profile
    override val database = profile.api.Database
      .forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  }
  val stored = new dal.MessagesDal(db)

  {
    log("Database initializing")
    import stored.db._
    import stored.db.profile.api._

    val dbio = DBIO.seq(
      stored.Messages.schema.create,
      stored.Messages += model.Message.now("hello world")
    )
    Await.ready(database.run(dbio), 5.seconds)
  }

  log("Application components initializing")
  val live = new dal.LiveMessages()
  val messageService = new service.MessageService(stored, live)
  val dashboard = new route.Dashboard(config.getString("title"))
  val messages = new route.Messages(messageService)

  val routes: Route = RouteConcatenation.concat(
    dashboard.route,
    messages.route
  )
  Http()
    .bindAndHandle(routes,
                   config.getString("address"),
                   config.getInt("port"))
    .map { binding =>
      log(s"Listening on ${binding.localAddress.toString}")
    }

  system.registerOnTermination(println("App stopping"))
}
