package crashbox.ci
package dal

import akka.NotUsed
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{
  BroadcastHub,
  Keep,
  Source,
  SourceQueueWithComplete
}
import model.Message

class LiveMessages(implicit materializer: Materializer) {

  private val (in: SourceQueueWithComplete[Message],
               out: Source[Message, NotUsed]) = Source
    .queue[Message](10, OverflowStrategy.dropTail)
    .toMat(BroadcastHub.sink[Message])(Keep.both)
    .run()

  def push(message: Message) = in.offer(message)
  def feed = out

}
