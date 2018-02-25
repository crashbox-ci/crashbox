package crashbox.ci
package curl

//import scala.scalanative.posix.s
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Promise}
import scala.scalanative.native._
import scala.util.{Failure, Success, Try}

object Http {
  import curl._

  final val MaxRead: CSize = 8192

  private def reader(data: Ptr[Byte],
                     size: CSize,
                     nmemb: CSize,
                     buffer: Ptr[Byte]): CSize = {
    val len = if (MaxRead > size * nmemb) size * nmemb else MaxRead
    string.memcpy(buffer, data, len)
    len
  }
  private val readerFunction: WriteFunction = CFunctionPtr.fromFunction4(reader)

  // TODO: check the efficieny of this. Do we really need that many intermediate buffers?
  def get(url: String): Try[String] = Zone { implicit z =>
    val curl: CURL = curl_easy_init()
    if (curl != null) {
      val builder = new ArrayBuffer[Byte]
      val buffer = alloc[Byte](MaxRead)

      curl_easy_setopt(curl, CURLoption.CURLOPT_URL, toCString(url))
      curl_easy_setopt(curl, CURLoption.CURLOPT_WRITEFUNCTION, readerFunction)
      curl_easy_setopt(curl, CURLoption.CURLOPT_WRITEDATA, buffer)
      val res = curl_easy_perform(curl)
      curl_easy_cleanup(curl)

      res match {
        case CURLcode.CURL_OK =>
          var i = 0
          while (i <= MaxRead && buffer(i) != 0) {
            builder += buffer(i)
            i += 1
          }
          Success(new String(builder.toArray, "utf-8"))

        case code =>
          Failure(new RuntimeException(s"cURL returned exit status $code"))
      }

    } else {
      Failure(new RuntimeException(s"cURL failed to initialize"))
    }
  }

}
@link("curl")
@extern
object curl {

  /*
   * CURL *curl = curl_easy_init();
   * if(curl) {
   *   CURLcode res;
   *   curl_easy_setopt(curl, CURLOPT_URL, "http://example.com");
   *   res = curl_easy_perform(curl);
   *   curl_easy_cleanup(curl);
   * }
   */

  type CURL = Ptr[CStruct0]

  /*
   * #define CURLOPTTYPE_LONG          0
   * #define CURLOPTTYPE_OBJECTPOINT   10000
   * #define CURLOPTTYPE_STRINGPOINT   10000
   * #define CURLOPTTYPE_FUNCTIONPOINT 20000
   * #define CURLOPTTYPE_OFF_T         30000
   */

  type CURLcode = CInt
  object CURLcode {
    final val CURL_OK: CInt = 0
  }

  type CURLoption = CInt
  object CURLoption {
    final val CURLOPT_WRITEDATA: CInt = 10001
    final val CURLOPT_URL: CInt = 10002
    final val CURLOPT_VERBOSE: CInt = 41
    final val CURLOPT_WRITEFUNCTION: CInt = 20011
  }

  type WriteFunction = CFunctionPtr4[
    Ptr[Byte], // data
    CSize, // size
    CSize, // nmemb
    Ptr[Byte], // userdata
    CSize // return
  ]

  def curl_easy_init(): CURL = extern

  def curl_easy_setopt(curl: CURL,
                       option: CURLoption,
                       parameter: CVararg*): CURLcode = extern

  def curl_easy_perform(curl: CURL): CURLcode = extern

  def curl_easy_cleanup(curl: CURL): Unit = extern

  def curl_easy_recv(curl: CURL,
                     buffer: Ptr[Byte],
                     buflen: CSize,
                     received_length: Ptr[CSize]): CURLcode = extern

  def curl_version(): CString = extern

}
