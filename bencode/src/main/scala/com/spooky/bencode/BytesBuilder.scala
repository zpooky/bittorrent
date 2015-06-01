package com.spooky.bencode

import scala.collection.mutable.ArrayBuilder

class BytesBuilder(buffer: ArrayBuilder[Byte]) {
  def +=(b: Byte): BytesBuilder = {
    buffer.+=(b)
    this
  }
  def toArray: Array[Byte] = buffer.result
}
object BytesBuilder {
  def apply(first: Byte) = new BytesBuilder(Array.newBuilder[Byte] += first)
}
