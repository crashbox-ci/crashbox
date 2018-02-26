package crashbox.ci
package service

import akka.NotUsed
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.Source
import model.Message
import dal._
import slick.basic.DatabasePublisher

import scala.concurrent.{ExecutionContext, Future}

class MessageService(stored: MessagesDal, live: LiveMessages) {
  import stored._
  import db._
  import stored.db.profile.api._

  def stream: Source[Message, NotUsed] = {
    val publisher = database.stream(Messages.result)
    Source.fromPublisher(publisher).concat(live.feed)
  }

  def push(message: Message)(implicit ec: ExecutionContext): Future[Message] = {
    database.run(Messages += message).flatMap(_ => live.push(message)).map(_ => message)
  }

}
