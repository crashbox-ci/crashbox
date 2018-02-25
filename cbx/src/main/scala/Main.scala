package crashbox.ci

import spray.json._

object Main extends DefaultJsonProtocol {

  case class A(x: Int)

  def main(args: Array[String]): Unit = {
    println(s"hello world ${A(2).toJson.toString}")
  }

}
