package com.spooky.bittorrent

import java.nio.ByteBuffer
import akka.util.ByteString
import akka.util.FakeBStrings

trait Showable {
  def toByteBuffer: ByteBuffer
  def toByteString: ByteString = BStrings(toByteBuffer)


//	def toBytes: Array[Byte]
//  def toByteBuffer: ByteBuffer = ByteBuffer.wrap(toBytes)
//  def toByteString: ByteString = FakeBStrings(toBytes)
}
