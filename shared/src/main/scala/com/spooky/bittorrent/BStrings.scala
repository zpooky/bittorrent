package com.spooky.bittorrent

import akka.util.ByteString
import java.nio.ByteBuffer
import akka.util.FakeBStrings

object BStrings {

  def apply(bb: ByteBuffer): ByteString = FakeBStrings.apply(bb.array)

}
