package crashbox.ci
package route

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes
import play.twirl.api.Html

trait TwirlSupport {

  /** Enables completing requests with html. */
  implicit val twirlHtml: ToEntityMarshaller[Html] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`text/html`) { h: Html =>
      h.toString
    }


}
