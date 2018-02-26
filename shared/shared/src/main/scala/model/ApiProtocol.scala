package crashbox.ci
package model

import java.util.UUID

import spray.json._

trait ApiProtocol extends DefaultJsonProtocol {

  implicit val uuidFormat: JsonFormat[UUID] = new Typeclass[UUID] {
    override def write(obj: UUID): JsValue = obj.toString.toJson
    override def read(json: JsValue): UUID =
      UUID.fromString(json.convertTo[String])
  }

  // add custom protocols here
}
