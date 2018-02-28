package crashbox.ci

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

trait Util {

  implicit class FutureOps[A](val future: Future[A]) {
    def await: Try[A] = {
      Await.ready(future, Duration.Inf)
      future.value.get
    }
  }

}
