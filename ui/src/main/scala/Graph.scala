package crashbox.ci

import crashbox.ci.model.ApiProtocol
import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket
import org.scalajs.dom.{Event, MessageEvent, console, document}
import spray.json._

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.{Failure, Success}

@JSExportTopLevel("Graph")
object Graph extends JsApp with ApiProtocol {

  def ws(path: String): String = {
    val scheme = dom.window.location.protocol match {
      case "http:" => "ws"
      case "https:" => "wss"
    }
    s"$scheme://${dom.window.location.host}$path"
  }


  case class A(x: Int)

  def main(args: Map[String, String]): Unit = {
    for (i <- 0 to 10) {
      val js = A(i).toJson.convertTo[A]
      val node = document.createElement("p"); // Create a <li> node
      val textnode = document.createTextNode(js.toString); // Create a text node
      node.appendChild(textnode); // Append the text to <li>
      env.root.appendChild(
        node
      )
    }

    Http.get[model.spec.Image]("/api").onComplete {
      case Success(m)   => console.info(m.toString)
      case Failure(err) => console.error(err.toString)
    }

    val socket = new WebSocket(ws("/messages/feed"))
    socket.onmessage = (e: MessageEvent) => {
      val str = e.data.asInstanceOf[String]
      val message = str.parseJson.convertTo[model.Message]

      val node = document.createElement("p")
      val textnode = document.createTextNode(message.content.toString)
      node.appendChild(textnode)
      env.root.appendChild(
        node
      )
    }
  }

}
