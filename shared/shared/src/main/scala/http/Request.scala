package crashbox.ci
package http

case class Request(
    method: String,
    url: String,
    headers: Map[String, String] = Map.empty,
    body: Option[Array[Byte]] = None
)
