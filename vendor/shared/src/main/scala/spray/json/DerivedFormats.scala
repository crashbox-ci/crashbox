package spray.json

import magnolia._

import scala.language.experimental.macros

/** Mixin that enables automatic derivation of JSON formats for any product
  * (case classes) or coproduct (sealed traits) types. */
trait DerivedFormats { self: BasicFormats =>
  type Typeclass[T] = JsonFormat[T]

  def combine[T](ctx: CaseClass[JsonFormat, T]): JsonFormat[T] =
    new JsonFormat[T] {
      override def write(value: T): JsValue =
        if (ctx.isObject) {
          JsString(ctx.typeName.short)
        } else {
          val fields: Seq[(String, JsValue)] = ctx.parameters.map { param =>
            param.label -> param.typeclass.write(param.dereference(value))
          }
          JsObject(fields: _*)
        }

      override def read(value: JsValue): T = value match {
        case obj: JsObject =>
          ctx.construct { param =>
            param.typeclass.read(obj.fields(param.label))
          }
        case JsString(str) if ctx.isObject && str == ctx.typeName.short =>
          ctx.rawConstruct(Seq.empty)

        case js =>
          deserializationError(
            s"Cannot read JSON '$js' as a ${ctx.typeName.full}")
      }
    }

  def dispatch[T](ctx: SealedTrait[JsonFormat, T]): JsonFormat[T] = {
    val typeFieldName = ctx.annotations
      .collectFirst {
        case g: gadt => g.typeFieldName
      }
      .getOrElse("type")

    new JsonFormat[T] {
      override def write(value: T): JsValue = ctx.dispatch(value) { sub =>
        sub.typeclass.write(sub.cast(value)) match {
          case obj: JsObject =>
            JsObject(
              (Map(typeFieldName -> JsString(sub.typeName.short)) ++
                obj.fields).toSeq: _*)
          case js => js
        }
      }

      override def read(js: JsValue): T = {
        val typeName: String = js match {
          case obj: JsObject if obj.fields.contains(typeFieldName) =>
            obj.fields(typeFieldName).convertTo[String]
          case JsString(str) =>
            str
          case _ =>
            deserializationError(
              s"Cannot deserialize JSON to ${ctx.typeName.full} " +
                "because serialized type cannot be determined.")
        }

        ctx.subtypes.find(_.typeName.short == typeName) match {
          case Some(tpe) => tpe.typeclass.read(js)
          case None =>
            deserializationError(
              s"Cannot deserialize JSON to ${ctx.typeName.full} " +
                s"because type '${typeName}' is unsupported.")
        }
      }
    }
  }

  implicit def gen[T]: JsonFormat[T] = macro Magnolia.gen[T]

}

object DerivedFormats extends DerivedFormats with BasicFormats
