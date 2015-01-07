package com.spooky.bittorrent.bencode

import spray.http.HttpData
import akka.util.ByteString

trait BStream {
  def headChar: Char
  def headByte: Byte
  def tail: BStream
  def isEmpty: Boolean
}

class StringBStream(string: String) extends BStream {
  def headChar = string.head
  def headByte: Byte = throw new RuntimeException("not supported")
  def tail: BStream = new StringBStream(string.tail)
  def isEmpty: Boolean = string.isEmpty
}

class ByteBStream(bytes: Array[Byte], index: Int) extends BStream {
  def headChar = headByte.asInstanceOf[Char]
  def headByte: Byte = bytes(index)
  def tail: BStream = new ByteBStream(bytes, index + 1)
  def isEmpty: Boolean = index >= bytes.length
}

object HttpDataBStream {
  def apply(data: HttpData.NonEmpty): HttpDataBStream = {
    val stream = data.toChunkStream(3)
    HttpDataBStream(stream)
  }
  def apply(stream: Stream[HttpData]): HttpDataBStream = new HttpDataBStream(stream.head.toByteString, 0, stream.tail)
}
class HttpDataBStream(chunk: ByteString, index: Int, nextChunk: Stream[HttpData]) extends BStream {
  def headChar: Char = headByte.toByte.asInstanceOf[Char]
  def headByte: Byte = chunk(index)
  def tail: BStream = {
    val nextIndex = index + 1
    if (nextIndex < chunk.length) {
      new HttpDataBStream(chunk, nextIndex, nextChunk)
    } else HttpDataBStream(nextChunk)
  }
  def isEmpty: Boolean = (index + 1) == chunk.length && nextChunk.isEmpty
}
