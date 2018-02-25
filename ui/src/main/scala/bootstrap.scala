package crashbox.ci

import org.scalajs.dom
import org.scalajs.dom.{console, html}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

trait JsApp {
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  private var _env: Environment = null
  implicit def env: Environment =
    if (_env != null) {
      _env
    } else {
      sys.error("Environment is unavailable because the application isn't initialized yet!")
    }

  lazy val cookies: Map[String, String] = dom.document.cookie
    .split(";")
    .map(_.split("="))
    .map { kv =>
      kv(0) -> kv(1)
    }
    .toMap

  @JSExport
  final def start(settings: js.Dynamic): Unit = {
    console.info("Initializing application...")

    console.info("Setting up environment...")
    _env = new Environment(
      root = settings.root.asInstanceOf[html.Element],
      baseUrl = settings.baseUrl.asInstanceOf[String]
    )

    console.info("Reading arguments...")
    val args: Map[String, String] =
      settings.args.asInstanceOf[js.Dictionary[Any]].mapValues(_.toString).toMap

    console.info("Initialization complete. Entering main...")
    main(args)
  }

  def main(args: Map[String, String]): Unit

}

/** Represents an application's environment
  * @param root The application's root element.
  * @param styleRoot An html 'style' tag to which app-specific styles are appended.
  * @param baseUrl Base URL. */
case class Environment(
                        root: html.Element,
                        baseUrl: String
                      )
