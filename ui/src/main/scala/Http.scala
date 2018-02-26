package crashbox.ci

import spray.json._

import scala.concurrent.{ExecutionContext, Future, Promise, TimeoutException}
import scala.scalajs.js

object Http {

  def get[A: JsonReader](url: String, headers: (String, String)*)(
      implicit ec: ExecutionContext): Future[A] = {
    val request = http.Request("GET", url, headers.toMap)
    http.send(request).flatMap { response =>
      if (200 <= response.statusCode && response.statusCode < 300) {
        val str = new String(response.body, "utf-8")
        Future { str.parseJson.convertTo[A] }
      } else {
        Future.failed(
          new RuntimeException(
            s"Non-successful response from the server: ${response.statusCode}"))
      }
    }
  }

}
