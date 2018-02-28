package xyz.driver.json

import spray.json._

import utest._

sealed trait Expr
case class Zero() extends Expr
case class Value(x: Int) extends Expr
case class Plus(lhs: Expr, rhs: Expr) extends Expr
case object One extends Expr

object CoproductTypeFormats
    extends TestSuite
    with FormatTests
    with DefaultJsonProtocol
    with DerivedFormats {

  val f = gen[Value]

  @gadt("kind")
  sealed abstract class Keyword(`type`: String)
  case class If(`type`: String) extends Keyword(`type`)

  sealed trait Enum
  case object A extends Enum
  case object B extends Enum

  val tests = Tests {

    "No-parameter case class child" - checkRoundtrip[Expr](
      Zero(),
      """{"type":"Zero"}"""
    )

    "Simple parameter case class child" - checkRoundtrip[Expr](
      Value(42),
      """{"type":"Value","x":42}"""
    )

    "Nested parameter case class child" - checkRoundtrip[Expr](
      Plus(Value(42), One),
      """{"type":"Plus","lhs":{"type":"Value","x":42},"rhs":"One"}"""
    )

    "Case object child" - checkRoundtrip[Expr](
      One,
      """"One""""
    )

    "GADT with type field alias" - checkRoundtrip[Keyword](
      If("class"),
      """{"kind":"If","type":"class"}"""
    )

    "Enum" - checkRoundtrip[List[Enum]](
      A :: B :: Nil,
      """["A", "B"]"""
    )
  }

}
