package crashbox.ci

import scala.collection.JavaConverters._
import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.net.URL
import java.nio.file.{Files, Paths}

import spray.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, Future}
import scala.util.{Failure, Success, Try}
import model.ApiProtocol

object Main extends ApiProtocol with Util {
  val cbx = cmd.Command(
    "cbx",
    cmd.Option("server", Some('s'), Some(cmd.Parameter("url"))),
    cmd.Command("completion"),
    cmd.Command("help"),
    cmd.Command("version", cmd.Option("verbose", Some('v'))),
    cmd.Command("message",
                cmd.Option("from-file", None, Some(cmd.Parameter("path"))),
                cmd.Parameter("message", false))
  )

  def fatal(message: String) = {
    System.err.println(message)
    sys.exit(1)
  }

  def main(args: Array[String]): Unit = cmd.parseOrExit(cbx, args) { command =>
    val server = command.arguments.getOrElse("server", "http://localhost:8080")
    command.subcommand match {
      case Some(cmd.CommandLine("version", args, _)) =>
        import BuildInfo._
        import NativeBuildInfo._
        if (args.contains("verbose")) {
          println(s"cbx $Version")
          println(s"curl ${http.CurlBackend.curlVersion}")
          println(s"compiled with Scala Native $NativeVersion on $Platform")

        } else {
          println(BuildInfo.Version)
        }
      case Some(cmd.CommandLine("completion", _, _)) =>
        println(cbx.completion)
      case Some(cmd.CommandLine("help", _, _)) =>
        println(cbx.usage)
      case Some(cmd.CommandLine("message", args, _)) =>
        val content = args.get("message") match {
          case Some(str) => str
          case None =>
            val path = args.getOrElse(
              "from-file",
              fatal("from-file must be specified if no message is given"))
            Files.readAllLines(Paths.get(path)).asScala.mkString("\n")
        }
        val request = http.Request(
          "POST",
          s"$server/messages",
          headers = Map(
            "Content-type" -> "application/json"
          ),
          body = model.Message.now(content).toJson.prettyPrint.getBytes("utf-8")
        )
        http.send(request).await match {
          case Success(str) =>
            println("Message delivered: " + new String(str.body, "utf-8"))
          case Failure(err) =>
            System.err.println(err.getMessage())
            sys.exit(1)
        }
    }
  }
}
