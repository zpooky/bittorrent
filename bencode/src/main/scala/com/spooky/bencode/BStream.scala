package com.spooky.bencode

import akka.util.ByteString
import scala.annotation.tailrec

trait BStream {
  def headChar: Char
  def headByte: Byte
  def tail: BStream
  def isEmpty: Boolean
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

object EmptyBSream extends BStream {
  def headChar: Char = throw new RuntimeException("empty.headChar")
  def headByte: Byte = throw new RuntimeException("empty.headByte")
  def tail: BStream = this
  def isEmpty: Boolean = true
}

class StringBStream(string: String) extends BStream {
  def headChar = string.head
  def headByte: Byte = throw new RuntimeException("not supported")
  def tail: BStream = {
    val tail = string.tail
    if (tail.isEmpty) {
      EmptyBSream
    } else new StringBStream(tail)
  }
  def isEmpty: Boolean = string.isEmpty
}

class ByteBStream(bytes: Array[Byte], index: Int) extends BStream {
  def headChar = headByte.asInstanceOf[Char]
  def headByte: Byte = bytes(index)
  def tail: BStream = {
    if (index + 1 == bytes.length) {
      EmptyBSream
    } else new ByteBStream(bytes, index + 1)
  }
  def isEmpty: Boolean = index >= bytes.length
}

class ByteStringBStream(data: ByteString, index: Int) extends BStream {
  def headChar = data(index).asInstanceOf[Char]
  def headByte: Byte = data(index)
  def tail: BStream = {
    if (index + 1 == data.length) {
      EmptyBSream
    } else new ByteStringBStream(data, index + 1)
  }
  def isEmpty: Boolean = data.length == index
}