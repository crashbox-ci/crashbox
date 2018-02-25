package crashbox.ci
package model

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import java.security.MessageDigest

object `package` {

  case class HashId(hash: String) extends AnyVal {
    def combine(other: HashId): HashId = HashId.fromString(hash + other.hash)
    override def toString = hash
  }

  object HashId {

    final val Zero = HashId(
      "0000000000000000000000000000000000000000000000000000000000000000")

    implicit val ordering: Ordering[HashId] =
      Ordering.by((hash: HashId) => hash.hash)

    private def bytesToHex(hash: Array[Byte]): String = {
      val hexString = new StringBuffer
      var i = 0
      while (i < hash.length) {
        val hex = Integer.toHexString(0xff & hash(i))
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
        i += 1;
      }
      hexString.toString
    }

    def fromBytes(bytes: Array[Byte]): HashId = {
      val digest = MessageDigest.getInstance("SHA-256")
      val encoded = digest.digest(bytes)
      HashId(bytesToHex(encoded))
    }

    def fromString(str: String): HashId = fromBytes(str.getBytes("utf-8"))

    def fromFile(file: Path): HashId = {
      val digest = MessageDigest.getInstance("SHA-256")
      Files.walk(file).filter(Files.isRegularFile(_)).forEach { f =>
        using(Files.newByteChannel(f)) { stream =>
          val chunk = ByteBuffer.allocateDirect(8192)
          while (stream.read(chunk) > 0) {
            digest.update(chunk)
          }
        }
      }
      HashId(bytesToHex(digest.digest()))
    }

    def combineOrdered(hashes: Traversable[HashId]) =
      hashes.toSeq.sorted.fold(Zero)(_ combine _)
  }

  def using[A >: Null <: Closeable, B](mkResource: => A)(action: A => B): B = {
    var instance: A = null
    try {
      instance = mkResource
      action(instance)
    } finally {
      if (instance != null) instance.close()
    }
  }

}
