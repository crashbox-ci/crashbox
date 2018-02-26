package crashbox.ci
package dal

import java.time.Instant
import java.util.UUID

import model.Message

class MessagesDal(val db: DatabaseConfig) {
  import db.profile.api._

  implicit val instantColumnType = MappedColumnType.base[Instant, Long](
    { instant =>
      instant.getEpochSecond
    }, { seconds =>
      Instant.ofEpochSecond(seconds)
    }
  )

  class Messages(tag: Tag) extends Table[Message](tag, "messages") {
    def id = column[UUID]("id", O.PrimaryKey)
    def created = column[Long]("created")
    def content = column[String]("content")
    def * =
      (id, created, content) <> ((Message.apply _).tupled, Message.unapply)
  }
  lazy val Messages = TableQuery[Messages]

}
