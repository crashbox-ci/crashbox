package crashbox.ci
package route

import akka.http.scaladsl.server.Directives
import model.ApiProtocol
import model.spec.Image
import spray.json.{DefaultJsonProtocol, DerivedFormats}

class Dashboard(title: String) extends Directives with TwirlSupport with ApiProtocol with SprayJsonSupport {

  val route = pathEndOrSingleSlash(
    complete(view.html.main(title))
  ) ~ path("api") {
    complete(Image.Docker("test"): Image)
  } ~ pathPrefix("assets") {
    getFromResourceDirectory("assets")
  }


}
