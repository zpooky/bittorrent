package com.spooky.cipher

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import java.nio.ByteBuffer
import akka.util.ByteString

sealed trait WriteCipher {
  def ignore(bb: ByteBuffer, length: Int): Unit
  def update(bs: Array[Byte]): Array[Byte]
  def update(bb: ByteBuffer): ByteString
}

class WriteRC4Cipher(writeKey: SecretKeySpec) extends RC4Cipher(writeKey, true) with WriteCipher {

}

object WritePlain extends WriteCipher {
  def ignore(bb: ByteBuffer, length: Int): Unit = {
    ???
  }

  def update(bb: ByteBuffer): ByteString = {
    ???
  }

  def update(bs: Array[Byte]): Array[Byte] = {
    ???
  }
}
