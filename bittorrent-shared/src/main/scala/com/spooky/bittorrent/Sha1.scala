package com.spooky.bittorrent

import java.security.MessageDigest
import java.nio.ByteBuffer
import scala.util.Random
import scala.annotation.tailrec
import org.apache.commons.codec.binary.Hex


sealed case class Sha1(raw: Array[Byte]) {
  override def toString: String = Hex.encodeHexString(raw)
  override def equals(o: Any): Boolean = o match {
    case os: Sha1 => MessageDigest.isEqual(raw, os.raw)
    case _        => false
  }
  def xor(o: Sha1): Array[Byte] = {
    assert(raw.length == o.raw.length)
      @tailrec
      def rec(i: Array[Byte], o: Array[Byte], result: Array[Byte], index: Int): Array[Byte] = index match {
        case n if n == i.length => result
        case n => {
          result(n) = (i(n) ^ o(n)).asInstanceOf[Byte]
          rec(i, o, result, n + 1)
        }
      }
    val result = new Array[Byte](raw.length)
    rec(raw, o.raw, result, 0)
  }
}

object Sha1 extends Algorithm(20) {
  def from(str: String): Sha1 = Sha1(MessageDigest.getInstance("sha1").digest(str.getBytes(UTF8)))
  //    def raw(buffer: ByteBuffer): Sha1 = {
  //      val raw = new Array[Byte](20)
  //      buffer.get(raw)
  //      Sha1(raw)
  //    }
  def from(str: String, key: SharedSecretKey): Sha1 = from(str.getBytes(UTF8), key.raw)
  def from(str: String, key: InfoHash): Sha1 = from(str.getBytes(UTF8), key.sum)
  def from(r1: Array[Byte], r2: Array[Byte]): Sha1 = {
    val digest = MessageDigest.getInstance("sha1")
    digest.update(r1)
    Sha1(digest.digest(r2))
  }
  def raw(buffer: ByteBuffer): Sha1 = {
    val r = Array.ofDim[Byte](20)
    buffer.get(r)
    Sha1(r)
  }
  def random: Sha1 = {
    val raw = Array.ofDim[Byte](20)
    Random.nextBytes(raw)
    Sha1(MessageDigest.getInstance("sha1").digest(raw))
  }
}
