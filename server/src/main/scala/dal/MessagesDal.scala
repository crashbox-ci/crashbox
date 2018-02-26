package crashbox.ci
package dal

import java.util.UUID
import model.Message

class MessagesDal(val db: DatabaseConfig) {
  import db.profile.api._

  class Messages(tag: Tag) extends Table[Message](tag, "accounts") {
    def id = column[UUID]("id", O.PrimaryKey)
    def created = column[Long]("created")
    def content = column[String]("content")
    def * = (id, created, content) <> ((Message.apply _).tupled, Message.unapply)
  }
  lazy val Messages = TableQuery[Messages]

}