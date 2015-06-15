package akka.util

import akka.util.ByteString.ByteString1

object FakeBStrings {
  def apply(bs: Array[Byte]): ByteString = ByteString1(bs)
}
