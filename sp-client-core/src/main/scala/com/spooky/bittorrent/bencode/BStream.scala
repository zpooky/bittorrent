package com.spooky.bittorrent.bencode

trait BStream {
  def headChar: Char
  def headByte: Byte
  def tail: BStream
  def isEmpty: Boolean
}

class StringBStream(string:String) extends BStream {
  def headChar = string.head
  def headByte: Byte = throw new RuntimeException("not supported")
  def tail: BStream = new StringBStream(string.tail)
  def isEmpty: Boolean = string.isEmpty
}

class ByteBStream(bytes: Array[Byte],index:Int) extends BStream {
  def headChar = headByte.asInstanceOf[Char]
  def headByte: Byte = bytes(index)
  def tail: BStream = new ByteBStream(bytes,index+1)
  def isEmpty: Boolean = index >= bytes.length
}