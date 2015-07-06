package com.spooky.bittorrent

import org.apache.commons.codec.binary.Hex
import java.util.Arrays
import java.security.MessageDigest
import java.nio.ByteBuffer

sealed case class InfoHash(raw: Array[Byte]) {
  override def toString: String = Hex.encodeHexString(raw)
  override def hashCode: Int = Arrays.hashCode(raw)
  override def equals(o: Any): Boolean = o match {
    case InfoHash(otherHash) => MessageDigest.isEqual(otherHash, raw)
    case _                   => false
  }
}
object InfoHash {
  def apply(sha1: Sha1): InfoHash = InfoHash(sha1.raw)
  def hex(hex: String): InfoHash = InfoHash(Hex.decodeHex(hex.toCharArray()))
  def parse(arr: Array[Byte]): InfoHash = {
    println(arr.length)
    assert(arr.length >= 20)
    InfoHash(arr.take(20))
  }
  def parse(buffer: ByteBuffer): InfoHash = {
    assert(buffer.remaining >= 20)
    val result = new Array[Byte](20)
    buffer.get(result)
    InfoHash(result)
  }
}
