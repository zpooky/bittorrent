package com.spooky.cipher

import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import akka.util.ByteString

sealed trait ReadCipher {
  def ignore(bb: ByteBuffer, length: Int): Unit
  def update(bs: Array[Byte]): Array[Byte]
  def update(bb: ByteBuffer): ByteString
}

final class ReadRC4Cipher(readKey: SecretKeySpec) extends RC4Cipher(readKey, false) with ReadCipher {
}

object ReadPlain extends ReadCipher {
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
