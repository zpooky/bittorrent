package com.spooky.cipher

import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import spooky.util.ByteString
import akka.util.FakeBStrings

sealed trait ReadCipher {
  def update(bs: Array[Byte]): Array[Byte]
  def update(bb: ByteBuffer): ByteString
  def update(bb: ByteString): ByteString
}

final class RC4ReadCipher(readKey: SecretKeySpec) extends RC4Cipher(readKey, false) with ReadCipher

object ReadPlain extends ReadCipher {

  def update(bb: ByteBuffer): ByteString = FakeBStrings(bb.duplicate)

  def update(bs: Array[Byte]): Array[Byte] = bs

  def update(bb: ByteString): ByteString = bb
}
