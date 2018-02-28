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

  val cbx = cmd.Command(
    "cbx",
    cmd.Command("help"),
    cmd.Command("version", cmd.Option("verbose", Some('v'))))

  def main(args: Array[String]): Unit =
    if (args.isEmpty) {
      http.send(http.Request("GET", "https://github.com")).await match {
        case Success(res) =>
          println(res.headers.mkString("\n"))
          println(new String(res.body, "utf-8"))
        case Failure(err) => println(err)
      }

      val request = http.Request("GET", "http://localhost:8080/api")

      http.send(request).await match {
        case Success(str) =>
          println(
            "Result: " + (new String(str.body, "utf-8")).parseJson
              .convertTo[model.spec.Image])
        case Failure(err) => println(err)
      }

      val content = if (args.length > 0) args(0) else "hello"
      val post = http.Request(
        "POST",
        "http://localhost:8080/messages",
        headers = Map(
          "Content-type" -> "application/json"
        ),
        body = model.Message.now(content).toJson.prettyPrint.getBytes("utf-8")
      )
      http.send(post).await match {
        case Success(str) =>
          println("Message delivered: " + new String(str.body, "utf-8"))
        case Failure(err) => println(err)
      }

    } else {
      try {
        cmd.parse(cbx, args).subcommand match {
          case Some(cmd.CommandLine("help", args, _)) =>
            println(cbx.usage)
          case Some(cmd.CommandLine("version", args, _)) =>
            if (args.contains("verbose")) {
              println(s"version: ${BuildInfo.version}")
              println(s"curl:    ${http.CurlBackend.curlVersion}")
            } else {
              println(BuildInfo.version)
            }
          case None =>
        }
      } catch {
        case err: cmd.ParseException =>
          println(err.getMessage)
          println(cbx.usage)
          System.exit(1)
      }
    }

}
