package crashbox.ci
package http

import scala.util.Try

trait Backend {
  def request(request: Request): Try[Response]
}
