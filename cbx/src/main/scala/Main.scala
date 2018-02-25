package crashbox.ci

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.net.URL

import spray.json._
import curl._

import scala.util.{Failure, Success}

object Main extends DefaultJsonProtocol {

  case class A(x: Int)

  def main(args: Array[String]): Unit = {
    Http.get("http://localhost:8080/api") match {
      case Success(str) => println(str.parseJson.convertTo[model.spec.Image])
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
