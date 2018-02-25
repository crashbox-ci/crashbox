package crashbox.ci
package model
package spec

sealed trait Source
object Source {
  // url can be name of local task or task defined in other pipeline
  // local: build-all
  // federated pipeline: crashboxserver/org/project/blob/master/crashbox.yml#build-all
  case class Task(url: String) extends Source
  case class External(url: String) extends Source
}

sealed trait Image
object Image {
  case class Docker(url: String) extends Image
}

case class Task(
                 name: String,
                 dependencies: Set[Source],
                 image: Image,
                 script: String
               )