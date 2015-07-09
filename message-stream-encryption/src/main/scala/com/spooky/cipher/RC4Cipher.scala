package com.spooky.cipher

import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import org.bouncycastle.crypto.engines.RC4Engine
import org.bouncycastle.crypto.params.KeyParameter
import akka.util.ByteString
import akka.util.FakeBStrings

private[cipher] abstract class RC4Cipher(key: SecretKeySpec, forEncryption: Boolean) {
  private val rc4Engine = new RC4Engine
  val params = new KeyParameter(key.getEncoded)
  rc4Engine.init(forEncryption, params)

  for (n <- 0 until 1024) {
    rc4Engine.returnByte(0)
  }

  def ignore(bb: ByteBuffer, length: Int): Unit = {
    for (n <- 0 until length) {
      rc4Engine.returnByte(bb.get)
    }
  }

  def update(bs: Array[Byte]): Array[Byte] = {
    if (bs.length > 0) {
      val result = Array.ofDim[Byte](bs.length)
      rc4Engine.processBytes(bs, 0, bs.length, result, 0)
      result
    } else Array.ofDim[Byte](0)
  }
  def update(bb: ByteBuffer): ByteString = {
    val result = Array.ofDim[Byte](bb.remaining)
    for (i <- 0 until result.length) {
      result(i) = rc4Engine.returnByte(bb.get)
    }
    FakeBStrings(result)
  }

  def update(bb: ByteString): ByteString = {
    FakeBStrings(update(bb.toArray))
  }

  def updateToBytes(bb: ByteBuffer): Array[Byte] = {
    val result = Array.ofDim[Byte](bb.remaining)
    for (i <- 0 until result.length) {
      result(i) = rc4Engine.returnByte(bb.get)
    }
    result
  }
}
