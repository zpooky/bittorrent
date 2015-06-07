package com.spooky.bencode

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
  override def toString: String = {
    val b = StringBuilder.newBuilder
    var s: BStream = this
    while (!s.isEmpty) {
      b.append(s.headChar)
      s = s.tail
    }
    b.toString
  }
}

class ByteStringBStream(data: ByteString, index: Int) extends BStream {
  def headChar = data(index).asInstanceOf[Char]
  def headByte: Byte = data(index)
  def tail: BStream = new ByteStringBStream(data, index + 1)
  def isEmpty: Boolean = data.length == index
}