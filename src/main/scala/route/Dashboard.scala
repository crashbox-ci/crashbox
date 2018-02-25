package crashbox.ci
package route

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.DefaultJsonProtocol

class Dashboard(title: String) extends Directives with TwirlSupport with DefaultJsonProtocol with SprayJsonSupport {

  val route = pathEndOrSingleSlash(
    complete(view.html.main(title))
  ) ~ pathPrefix("assets") {
    getFromResourceDirectory("assets")
  }


}
