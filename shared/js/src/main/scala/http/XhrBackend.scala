package crashbox.ci
package http

import org.scalajs.dom.raw.Blob
import org.scalajs.dom.{ErrorEvent, Event, XMLHttpRequest}
import spray.json.JsonReader

import scala.concurrent.{ExecutionContext, Future, Promise, TimeoutException}
import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Int8Array, Uint8Array}

trait XhrBackend extends Backend {

  def send(request: Request): Future[Response] = {
    val promise = Promise[Response]
    val xhr = new XMLHttpRequest()

    xhr.open(request.method, request.url)
    xhr.responseType = "arraybuffer"
    for ((name, value) <- request.headers) {
      xhr.setRequestHeader(name, value)
    }
    request.body match {
      case Some(data) => ???

      case None =>
        xhr.send()
    }

    xhr.onload = (e: Event) => {
      val body: Array[Byte] = if (!js.isUndefined(xhr.response)) {
        val buffer = new Int8Array(xhr.response.asInstanceOf[ArrayBuffer])
        buffer.toArray
      } else {
        Array.empty[Byte]
      }

      val response = Response(
        xhr.status,
        Map.empty,
        body
      )
      promise.success(response)
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

}
