package crashbox.ci
package route

import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, Sink}
import service.MessageService
import model.{ApiProtocol, Message}
import spray.json._

class Messages(service: MessageService)
    extends Directives
    with ApiProtocol
    with SprayJsonSupport {

  def route = pathPrefix("messages") {
    path("feed") {
      val toWebSocket = Flow[Message].map { message =>
        TextMessage(message.toJson.compactPrint)
      }
      handleWebSocketMessages(
        Flow.fromSinkAndSource(Sink.ignore, service.stream).via(toWebSocket)
      )
    } ~ pathEndOrSingleSlash {
      extractExecutionContext { implicit ec =>
        post {
          entity(as[Message]) { message =>
            complete(service.push(message))
          }
        }
      }
    }
  }

}
