package crashbox.ci
package http

import scala.util.Try

case class Request(
    method: String,
    url: String,
    headers: Map[String, String] = Map.empty,
    body: Option[Array[Byte]] = None
)

case class Response(statusCode: Int,
                    headers: Map[String, String],
                    body: Array[Byte])

object `package` {

  def request(request: Request): Try[Response] =
    backend.CurlBackend.request(request)

}