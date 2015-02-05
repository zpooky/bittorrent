package com.spooky.bittorrent

import java.nio.ByteBuffer
import java.security.MessageDigest

class ImmutableByteBuffer private (private val buffer: ByteBuffer) {
  def toArray: Array[Byte] = buffer.array()
  def toByteBuffer: ByteBuffer = buffer.duplicate()
  def length: Int = buffer.remaining
  override def equals(o: Any): Boolean = o match {
    case other: ImmutableByteBuffer => buffer == other.buffer
    case other: ByteBuffer          => buffer == other
    case other: Array[Byte]         => MessageDigest.isEqual(buffer.array(), other)
  }
  override def toString:String = buffer.toString
}
object ImmutableByteBuffer {
  def apply(buffer: ByteBuffer): ImmutableByteBuffer = new ImmutableByteBuffer(buffer.slice())
  def apply(buffer: Array[Byte]): ImmutableByteBuffer = new ImmutableByteBuffer(ByteBuffer.wrap(buffer))
}
