package akka.util

import spooky.util.ByteString
import java.nio.ByteBuffer

object FakeBStrings {
  def apply(bs: Array[Byte]): spooky.util.ByteString = spooky.util.ByteString(bs)
  def apply(bg: ByteBuffer): spooky.util.ByteString = spooky.util.ByteString(bg.array()) //TODO
}
