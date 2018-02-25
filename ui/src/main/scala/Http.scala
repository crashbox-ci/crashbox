package crashbox.ci

import org.scalajs.dom.{ErrorEvent, Event, XMLHttpRequest}
import spray.json.{JsonReader, _}

import scala.concurrent.{ExecutionContext, Future, Promise, TimeoutException}
import scala.scalajs.js

object Http {

  private def request[A: JsonReader](prepareAndSend: XMLHttpRequest => Unit)(
      implicit ec: ExecutionContext): Future[A] = {
    val promise = Promise[A]
    val xhr = new XMLHttpRequest()

    prepareAndSend(xhr)

    xhr.onload = (e: Event) => {
      if (200 <= xhr.status && xhr.status < 300) {
        promise.success(xhr.responseText.parseJson.convertTo[A])
      } else {
        promise.failure(
          new RuntimeException(
            s"Non-successful response from the server: ${xhr.statusText}"))
      }
    }
    xhr.onerror = (e: ErrorEvent) => {
      promise.failure(new RuntimeException(s"XHR error: ${e.message}"))
    }
    xhr.ontimeout = (e: Event) => {
      promise.failure(
        new TimeoutException(s"Request timed out: ${xhr.statusText}"))
    }
    promise.future
  }

  def get[A: JsonReader](url: String, headers: (String, String)*)(
      implicit ec: ExecutionContext): Future[A] = request[A] { xhr =>
    xhr.open("GET", url)
    for ((name, value) <- headers) { xhr.setRequestHeader(name, value) }
    xhr.send()
  }

  def post[A: JsonReader](url: String, headers: (String, String)*)(
      body: js.Any)(implicit ec: ExecutionContext): Future[A] =
    request[A] { xhr =>
      xhr.open("POST", url)
      for ((name, value) <- headers) { xhr.setRequestHeader(name, value) }
      xhr.send(body)
    }

}
