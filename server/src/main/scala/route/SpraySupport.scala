package crashbox.ci
package route


import java.nio.charset.StandardCharsets

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.{FromByteStringUnmarshaller, FromEntityUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import akka.util.ByteString
import spray.json.ParserInput.IndexedBytesParserInput
import spray.json.{JsonReader, _}

/**
  * A trait providing automatic to and from JSON marshalling/unmarshalling using an in-scope *spray-json* protocol.
  */
trait SprayJsonSupport {

  implicit def sprayJsonUnmarshallerConverter[T](reader: JsonReader[T]): FromEntityUnmarshaller[T] =
    sprayJsonUnmarshaller(reader)

  implicit def sprayJsonUnmarshaller[T](implicit reader: JsonReader[T]): FromEntityUnmarshaller[T] =
    sprayJsValueUnmarshaller.map(reader.read)

  implicit def sprayJsValueUnmarshaller: FromEntityUnmarshaller[JsValue] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .andThen(sprayJsValueByteStringUnmarshaller)

  implicit def sprayJsValueByteStringUnmarshaller[T]: FromByteStringUnmarshaller[JsValue] =
    Unmarshaller.withMaterializer[ByteString, JsValue](_ ⇒ implicit mat ⇒ { bs ⇒
      // .compact so addressing into any address is very fast (also for large chunks)
      // TODO we could optimise ByteStrings to better handle linear access like this (or provide ByteStrings.linearAccessOptimised)
      // TODO IF it's worth it.
      val parserInput = new SprayJsonByteStringParserInput(bs.compact)
      FastFuture.successful(JsonParser(parserInput))
    })
  implicit def sprayJsonByteStringUnmarshaller[T](implicit reader: JsonReader[T]): FromByteStringUnmarshaller[T] =
    sprayJsValueByteStringUnmarshaller[T].map(jsonReader[T].read)

  //#sprayJsonMarshallerConverter
  implicit def sprayJsonMarshallerConverter[T](writer: JsonWriter[T])(implicit printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[T] =
    sprayJsonMarshaller[T](writer, printer)
  //#sprayJsonMarshallerConverter
  implicit def sprayJsonMarshaller[T](implicit writer: JsonWriter[T], printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[T] =
    sprayJsValueMarshaller compose writer.write
  implicit def sprayJsValueMarshaller(implicit printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[JsValue] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(printer)
}

object SprayJsonSupport extends SprayJsonSupport

private final class SprayJsonByteStringParserInput(bytes: ByteString) extends IndexedBytesParserInput {
  protected def byteAt(offset: Int): Byte = bytes(offset)

  override def length: Int = bytes.size
  override def sliceString(start: Int, end: Int): String =
    bytes.slice(start, end - start).decodeString(StandardCharsets.UTF_8)
  override def sliceCharArray(start: Int, end: Int): Array[Char] =
    StandardCharsets.UTF_8.decode(bytes.slice(start, end).asByteBuffer).array()
}