package akka.util

import akka.util.ByteString.ByteString1
import java.nio.ByteBuffer
import akka.util.ByteString.ByteString1C

object FakeBStrings {
  def apply(bs: Array[Byte]): ByteString = ByteString1(bs)
  def apply(bg: ByteBuffer): ByteString = ByteString1(bg.array()) //TODO
}
