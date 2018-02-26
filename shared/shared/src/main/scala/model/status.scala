package crashbox.ci
package model
package status

import java.time.Instant

case class Task(
    name: String,
    scheduleTime: Instant,
    prev: Option[HashId],
    result: Option[HashId],
    script: () => HashId,
    parents: Int,
    children: Set[String]
) {
  lazy val id =
    HashId.fromString(name).combine(HashId.fromString(scheduleTime.toString))
}

object All {
  import scala.collection.mutable.HashMap

  val heads = HashMap.empty[String, HashId]
  val graph = HashMap.empty[HashId, Task]

  /*
  recursive_execute T {
    atomic { if T.count++ < T.indeg then return }
    execute T
      parallel foreach T' in T.succ
    recursive_execute T'
  }*/

  def schedule(nodeId: HashId) = {
    val node = graph(nodeId)
    val task = node.copy(scheduleTime = Instant.now(),
                         prev = Some(nodeId),
                         result = None)
    ???
  }

  def run(nodeId: HashId) = {
    val node = graph(nodeId)
    synchronized {
      //
    }
  }
}
