package com.spooky.bittorrent

import scala.annotation.tailrec
import java.security.MessageDigest
import java.util.Arrays
import org.apache.commons.codec.binary.Hex
import java.nio.ByteBuffer

sealed case class Checksum(sum: Array[Byte], algorithm: Algorithm) {
  def check(other: Array[Byte]): Boolean = {
    check(other.length, other)
  }
  def check(length: Int, other: Array[Byte]*): Boolean = {
      @tailrec
      def rec(length: Int, other: Seq[Array[Byte]], index: Int, digest: MessageDigest): Array[Byte] = {
        if (other.length - 1 == index) {
          digest.update(other(index), 0, length)
          digest.digest
        } else {
          digest.update(other(index))
          rec(length, other, index + 1, digest)
        }
      }
    val digester = MessageDigest.getInstance(algorithm.toString)
    MessageDigest.isEqual(sum, rec(length, other, 0, digester))
  }
  def compare(other: Array[Byte]): Boolean = MessageDigest.isEqual(sum, other)
  override def toString: String = Hex.encodeHexString(sum)
  override def hashCode: Int = Arrays.hashCode(sum)
  override def equals(o: Any): Boolean = o match {
    case Checksum(otherHash, Sha1) => MessageDigest.isEqual(otherHash, sum)
    case _                         => false
  }
}
object Checksum {
  def parse(raw: ByteBuffer, algorithm: Algorithm): Checksum = {
    val checksum = Array.ofDim[Byte](algorithm.bytes)
    for (n <- 0 until algorithm.bytes) {
      checksum(n) = raw.get
    }
    Checksum(checksum, algorithm)
  }
}
