package spooky.util

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.Buffer
import java.util.Arrays

object ByteString {
  private val UTF8 = Charset.forName("UTF8")
  def apply(str: String): ByteString = {
    val result = str.getBytes(UTF8)
    ByteString(result, result.length)
  }
  def apply(arr: Array[Byte]): ByteString = ByteString(arr, arr.length, 0)
  def apply(bb: ByteBuffer): ByteString = {
    val result = bb.array()
    ByteString(result, result.length)
  }
  def copy(bb: Buffer): ByteString = copy(bb.asInstanceOf[ByteBuffer])
  def copy(bb: ByteBuffer): ByteString = {
    val result = Array.ofDim[Byte](bb.remaining)
    bb.get(result)
    ByteString(result, result.length)
  }
}
case class ByteString(arr: Array[Byte], roof: Int, index: Int = 0) {
  def apply(index: Int): Byte = arr(this.index + index)
  def length: Int = roof - index
  def toArray: Array[Byte] = {
    val result = Array.ofDim[Byte](roof - index)
    System.arraycopy(arr, index, result, 0, result.length)
    result
  }
  def toByteBuffer: ByteBuffer = ???
  def take(n: Int): ByteString = {
    ByteString(arr, Math.min(arr.length, index + n))
  }
  def isEmpty: Boolean = roof == index
  def head: Byte = arr(index)

  override def toString:String = {
    s"index: $index roof: $roof | ${Arrays.toString(arr)}"
  }
}
