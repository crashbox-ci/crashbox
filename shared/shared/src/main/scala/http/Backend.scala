package crashbox.ci
package http

import scala.concurrent.Future

trait Backend {
  def send(request: Request): Future[Response]
}
