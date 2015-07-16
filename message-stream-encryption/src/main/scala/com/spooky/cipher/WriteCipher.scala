package com.spooky.cipher

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import java.nio.ByteBuffer
import spooky.util.ByteString
import akka.util.FakeBStrings

sealed trait WriteCipher {
  def update(bs: Array[Byte]): Array[Byte]
  def update(bb: ByteBuffer): ByteString
  def update(bb: ByteString): ByteString
  def updateToBytes(bb: ByteBuffer): Array[Byte]
}

class RC4WriteCipher(writeKey: SecretKeySpec) extends RC4Cipher(writeKey, true) with WriteCipher

object WritePlain extends WriteCipher {
  def update(bb: ByteBuffer): ByteString = FakeBStrings(bb.duplicate)

  def update(bs: Array[Byte]): Array[Byte] = bs

  def update(bb: ByteString): ByteString = bb

  def updateToBytes(bb: ByteBuffer): Array[Byte] = bb.array
}
