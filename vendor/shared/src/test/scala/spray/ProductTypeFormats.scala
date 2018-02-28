package xyz.driver.json

import spray.json._

import utest._

object ProductTypeFormats
    extends TestSuite
    with FormatTests
    with DerivedFormats
    with DefaultJsonProtocol {

  case class A()
  case class B(x: Int, b: String, mp: Map[String, Int])
  case class C(b: B)
  case object D
  case class E(d: D.type)
  case class F(x: Int)

  // custom format fqor F, that inverts the value of parameter x
  implicit val fFormat: JsonFormat[F] = new JsonFormat[F] {
    override def write(f: F): JsValue = JsObject("x" -> JsNumber(-f.x))

    override def read(js: JsValue): F =
      F(-js.asJsObject.fields("x").convertTo[Int])
  }

  val tests = Tests {
    "No-parameter product" - checkRoundtrip(A(), "{}")

    "Simple parameter product" - checkRoundtrip(
      B(42, "Hello World", Map("a" -> 1, "b" -> -1024)),
      """{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } }"""
    )

    "Nested parameter product" - checkRoundtrip(
      C(B(42, "Hello World", Map("a" -> 1, "b" -> -1024))),
      """{"b" :{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } } }"""
    )

    "Case object" - checkRoundtrip(
      D,
      """"D""""
    )

    "Case object as parameter" - checkRoundtrip(
      E(D),
      """{"d":"D"}"""
    )

    "Overriding with a custom format" - checkRoundtrip(
      F(2),
      """{"x":-2}"""
    )
  }

}
