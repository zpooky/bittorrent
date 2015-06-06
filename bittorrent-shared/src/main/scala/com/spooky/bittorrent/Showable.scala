package com.spooky.bittorrent

import java.nio.ByteBuffer
import akka.util.ByteString

trait Showable {
  def toByteBuffer: ByteBuffer
  def toByteString: ByteString = ByteString(toByteBuffer)
}