package com.spooky.bittorrent.bencode

import com.spooky.bencode.BStream
import akka.util.ByteString
import spray.http.HttpData

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
  def isEmpty: Boolean = (index + 1) >= chunk.length && nextChunk.isEmpty
}
