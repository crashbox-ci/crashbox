package crashbox.ci

import crashbox.ci.model.ApiProtocol
import org.scalajs.dom.{console, document}
import spray.json._

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.{Failure, Success}

@JSExportTopLevel("Graph")
object Graph extends JsApp with ApiProtocol {

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

    Http.get[model.spec.Image.Docker]("/api").onComplete {
      case Success(m)   => console.info(m.toString)
      case Failure(err) => console.error(err.toString)
    }

  }

}
