package crashbox.ci

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.net.URL

import spray.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, Future}
import scala.util.{Failure, Success, Try}
import model.ApiProtocol

object Main extends ApiProtocol {

  implicit class FutureOps[A](val future: Future[A]) extends AnyVal {
    def await: Try[A] = {
      Await.ready(future, Duration.Inf)
      future.value.get
    }
  }

  def main(args: Array[String]): Unit = {

    http.send(http.Request("GET", "https://github.com")).await match {
      case Success(str) => println(new String(str.body, "utf-8"))
      case Failure(err) => println(err)
    }

    val request = http.Request("GET", "http://localhost:8080/api")

    http.send(request).await match {
      case Success(str) => println("Result: " + new String(str.body, "utf-8"))
      case Failure(err) => println(err)
    }

    val content = if (args.length > 0) args(0) else "hello"
    val post = http.Request("POST", "http://localhost:8080/messages",
      headers = Map(
        "Content-type" -> "application/json"
      ),
      body = model.Message.now(content).toJson.prettyPrint.getBytes("utf-8")
    )
    http.send(post).await match {
      case Success(str) => println("Message delivered: " + new String(str.body, "utf-8"))
      case Failure(err) => println(err)
    }

  }

}
