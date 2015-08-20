package com.spooky.cipher

import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import spooky.util.ByteString
import akka.util.FakeBStrings

sealed trait ReadCipher {
  def update(bs: Array[Byte]): Array[Byte]
  def update(bb: ByteBuffer): ByteString
  def update(bs: ByteString): ByteString
  def updateBB(bs: ByteString): ByteBuffer
}

final class RC4ReadCipher(readKey: SecretKeySpec) extends RC4Cipher(readKey, false) with ReadCipher

object ReadPlain extends ReadCipher {

  def update(bb: ByteBuffer): ByteString = FakeBStrings(bb.duplicate)

  def update(bs: Array[Byte]): Array[Byte] = bs

  def update(bs: ByteString): ByteString = bs

  def updateBB(bs: ByteString): ByteBuffer = bs.toByteBuffer
}
