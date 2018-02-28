package crashbox.ci
package model

import java.time.Instant
import java.util.UUID

case class Message(id: UUID, timestamp: Long, content: String)

object Message {
  def now(content: String): Message =
    Message(UUID.randomUUID(), System.currentTimeMillis(), content)
}
