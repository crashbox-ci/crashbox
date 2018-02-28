package crashbox.ci

import java.time.Instant

import crashbox.ci.model.ApiProtocol
import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket
import org.scalajs.dom.{Event, MessageEvent, console, document}
import org.singlespaced.d3js.d3
import org.singlespaced.d3js.Ops._
import spray.json._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.{Failure, Success}

@JSExportTopLevel("UiMain")
object UiMain extends JsApp with ApiProtocol {

  def ws(path: String): String = {
    val scheme = dom.window.location.protocol match {
      case "http:"  => "ws"
      case "https:" => "wss"
    }
    s"$scheme://${dom.window.location.host}$path"
  }

  case class A(x: Int)

  def main(args: Map[String, String]): Unit = {
    Http.get[model.spec.Image]("/api").onComplete {
      case Success(m)   => console.info(m.toString)
      case Failure(err) => console.error(err.toString)
    }

    val indicator = {
      val node = document.createElement("p")
      env.root.appendChild(node)
      node
    }

    val display = {
      val node = document.createElement("ul")
      env.root.appendChild(node)
      node
    }
    var head: dom.Element = null
    def insert(node: dom.Element): Unit = {
      if (head == null) {
        display.appendChild(node)
      } else {
        display.insertBefore(node, head)
      }
      head = node
    }

    val socket = new WebSocket(ws("/messages/feed"))
    socket.onmessage = (e: MessageEvent) => {
      val str = e.data.asInstanceOf[String]
      val message = str.parseJson.convertTo[model.Message]

      val time = (new js.Date(message.timestamp)).toISOString()

      val line = s"[$time] ${message.content}"

      val node = document.createElement("li")
      val textnode = document.createTextNode(line)
      node.appendChild(textnode)
      insert(node)
    }

    socket.onopen = _ => {
      indicator.textContent = "live: ✔"
    }

    socket.onclose = _ => {
      indicator.textContent = "live: ✗"
    }

  }

}
