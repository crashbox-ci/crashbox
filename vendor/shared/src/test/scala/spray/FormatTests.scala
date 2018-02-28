package xyz.driver.json

import spray.json._
import utest._

trait FormatTests {

  def checkRoundtrip[A: JsonFormat](a: A, expectedJson: String) = {
    val expected: JsValue = expectedJson.parseJson
    assert(a.toJson == expected)

    val reread = a.toJson.convertTo[A]
    assert(reread == a)
  }

}
