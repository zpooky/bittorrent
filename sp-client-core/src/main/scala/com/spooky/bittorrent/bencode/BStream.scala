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