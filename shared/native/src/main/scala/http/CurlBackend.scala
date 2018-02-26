package crashbox.ci
package http

import curl._
import curlh._

import scala.collection.Map
import scala.scalanative.native._
import scala.util.{Failure, Success, Try}

object CurlBackend {

  type ResponseHolder = CStruct2[CSize, Ptr[Byte]]

  implicit class UserDataOps(val self: Ptr[ResponseHolder]) extends AnyVal {
    @inline def size: CSize = !(self._1)
    @inline def size_=(value: CSize): Unit = !(self._1) = value
    @inline def buffer: Ptr[Byte] = !(self._2)
    @inline def buffer_=(value: Ptr[Byte]): Unit = !(self._2) = value
  }

  private def receive(data: Ptr[Byte],
                      size: CSize,
                      nmemb: CSize,
                      voidptr: Ptr[Byte]): CSize = {
    val userData: Ptr[ResponseHolder] = voidptr.cast[Ptr[ResponseHolder]]
    userData.size = size * nmemb
    userData.buffer = stdlib.malloc(userData.size)
    if (userData.buffer != null) {
      string.memcpy(userData.buffer, data, userData.size)
      userData.size
    } else {
      0
    }
  }
  private val receivePtr: WriteFunction = CFunctionPtr.fromFunction4(receive)

  private def chain[A](success: A)(calls: (() => A)*) = {
    var result: A = success
    for (c <- calls if result == success) {
      result = c()
    }
    result
  }

  private def request(request: Request)(implicit z: Zone): Try[Response] = {
    val curl: CURL = curl_easy_init()
    if (curl != null) {
      val errorBuffer = stackalloc[Byte](CURL_ERROR_SIZE)
      errorBuffer(0) = 0
      val userData: Ptr[ResponseHolder] = stackalloc[ResponseHolder](1)
      userData.size = 0

      val curlResult = chain(CURLcode.CURL_OK)(
        () =>
          curl_easy_setopt(curl, CURLoption.CURLOPT_ERRORBUFFER, errorBuffer),
        () =>
          curl_easy_setopt(curl,
                           CURLoption.CURLOPT_CUSTOMREQUEST,
                           toCString(request.method)),
        () =>
          curl_easy_setopt(curl,
                           CURLoption.CURLOPT_URL,
                           toCString(request.url)),
        () =>
          request.body match {
            case Some(body) =>
              val buffer = ArrayUtils.toBuffer(body)
              curl_easy_setopt(curl, CURLoption.CURLOPT_POSTFIELDS, buffer)
              curl_easy_setopt(curl,
                               CURLoption.CURLOPT_POSTFIELDSIZE,
                               body.size)
            case None => CURLcode.CURL_OK
        },
        () =>
          curl_easy_setopt(curl, CURLoption.CURLOPT_WRITEFUNCTION, receivePtr),
        () => curl_easy_setopt(curl, CURLoption.CURLOPT_WRITEDATA, userData),
        () => curl_easy_perform(curl)
      )

      val result = curlResult match {
        case CURLcode.CURL_OK =>
          val responseCode = stackalloc[Long](1)
          curl_easy_getinfo(curl, CURLINFO.CURLINFO_RESPONSE_CODE, responseCode)

          Success(
            Response(
              statusCode = (!responseCode).toInt,
              headers = Map.empty,
              body = ArrayUtils.toArray(userData.buffer, userData.size)
            ))

        case code =>
          val message = curl_easy_strerror(curl, code)
          Failure(
            new RuntimeException(
              s"${fromCString(errorBuffer)} (curl exit status $code)"))
      }
      if (userData.size != 0) {
        stdlib.free(userData.buffer)
      }
      curl_easy_cleanup(curl)
      result
    } else {
      Failure(new RuntimeException(s"curl failed to initialize"))
    }
  }

}

trait CurlBackend extends Backend {
  def request(req: Request): Try[Response] = Zone { implicit z =>
    CurlBackend.request(req)
  }
}
