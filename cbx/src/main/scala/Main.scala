package crashbox.ci

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.net.URL

import spray.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, Future}
import scala.util.{Failure, Success, Try}

object Main extends DefaultJsonProtocol {

  implicit class FutureOps[A](val future: Future[A]) extends AnyVal {
    def await: Try[A] = {
      Await.ready(future, Duration.Inf)
      future.value.get
    }
  }

  case class A(x: Int)

  def main(args: Array[String]): Unit = {
    val request = http.Request("GET", "http://localhost:8080/api")

    http.send(request).await match {
      case Success(str) => println("Result: " + new String(str.body, "utf-8"))
      case Failure(err) => println(err)
    }

    //println(s"hello world ${A(2).toJson.toString}")
    //val conn = url.openConnection()
    //val reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))
    /*
      String inputLine;
      while ((inputLine = br.readLine()) != null) {
        System.out.println(inputLine);
      }
      br.close();

      System.out.println("Done");

    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }*/

  }

}
