package crashbox.ci

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Main extends App {
  def log(message: String) = System.err.println(message)

  log("App starts")

  log("System components initializing")
  implicit val system = ActorSystem("authentication")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  log("Configuration initializing")
  val config = system.settings.config.getConfig("crashboxd")
  val profile = slick.jdbc.H2Profile
  val db = profile.api.Database
    .forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  log("Application components initializing")
  val dashboard = new route.Dashboard(config.getString("title"))
  Http().bindAndHandle(dashboard.route, config.getString("address"), config.getInt("port")).map{binding =>
    log(s"Listening on ${binding.localAddress.toString}")
  }

  system.registerOnTermination(println("App stopping"))
}
