package crashbox.ci
package route

import akka.http.scaladsl.server.Directives
import crashbox.ci.model.ApiProtocol
import crashbox.ci.model.spec.Image

class Dashboard(title: String)
    extends Directives
    with TwirlSupport
    with ApiProtocol
    with SprayJsonSupport {

  val route = pathEndOrSingleSlash(
    complete(view.html.main(title))
  ) ~ path("api") {
    complete(Image.Docker("test"): Image)
  } ~ pathPrefix("assets") {
    getFromResourceDirectory("assets")
  }

}
