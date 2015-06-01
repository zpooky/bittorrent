package com.spooky

import java.security.MessageDigest
import java.nio.charset.Charset
import java.nio.ByteBuffer
import org.apache.commons.codec.binary.Hex
import scala.util.Random
import scala.util.Try
import scala.annotation.tailrec
import java.math.BigInteger
import com.spooky.bittorrent.mse.Rc4Key

object rc4 {
  private val dh_prime = Array[Byte]( //
    //
    0xFF.asInstanceOf[Byte], 0xFF.asInstanceOf[Byte], 0xFF.asInstanceOf[Byte], 0xFF.asInstanceOf[Byte], 0xFF.asInstanceOf[Byte], 0xFF.asInstanceOf[Byte], 0xFF.asInstanceOf[Byte], 0xFF.asInstanceOf[Byte], 0xC9.asInstanceOf[Byte], 0x0F, 0xDA.asInstanceOf[Byte], 0xA2.asInstanceOf[Byte], //
    0x21, 0x68, 0xC2.asInstanceOf[Byte], 0x34, 0xC4.asInstanceOf[Byte], 0xC6.asInstanceOf[Byte], 0x62, 0x8B.asInstanceOf[Byte], 0x80.asInstanceOf[Byte], 0xDC.asInstanceOf[Byte], 0x1C, 0xD1.asInstanceOf[Byte], //
    0x29, 0x02, 0x4E, 0x08, 0x8A.asInstanceOf[Byte], 0x67, 0xCC.asInstanceOf[Byte], 0x74, 0x02, 0x0B, 0xBE.asInstanceOf[Byte], 0xA6.asInstanceOf[Byte], //
    0x3B, 0x13, 0x9B.asInstanceOf[Byte], 0x22, 0x51, 0x4A, 0x08, 0x79, 0x8E.asInstanceOf[Byte], 0x34, 0x04, 0xDD.asInstanceOf[Byte], //
    0xEF.asInstanceOf[Byte], 0x95.asInstanceOf[Byte], 0x19, 0xB3.asInstanceOf[Byte], 0xCD.asInstanceOf[Byte], 0x3A, 0x43, 0x1B, 0x30, 0x2B, 0x0A, 0x6D, //
    0xF2.asInstanceOf[Byte], 0x5F, 0x14, 0x37, 0x4F, 0xE1.asInstanceOf[Byte], 0x35, 0x6D, 0x6D, 0x51, 0xC2.asInstanceOf[Byte], 0x45, //
    0xE4.asInstanceOf[Byte], 0x85.asInstanceOf[Byte], 0xB5.asInstanceOf[Byte], 0x76, 0x62, 0x5E, 0x7E, 0xC6.asInstanceOf[Byte], 0xF4.asInstanceOf[Byte], 0x4C, 0x42, 0xE9.asInstanceOf[Byte], //
    0xA6.asInstanceOf[Byte], 0x3A, 0x36, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x05, 0x63)
  //  val p = new java.math.BigInteger(dh_prime).abs()

  private val UTF8 = Charset.forName("UTF8")

  object Rc4 {
    def apply(incomming: Rc4Key, outgoing: Rc4Key): Codec = new Rc4Codec(incomming, outgoing)

    //    def incomming(incomming: Rc4Key): Decrypt = new Decrypt {
    //
    //      private val key = init(incomming.raw)
    //
    //      def decrypt(buffer: Array[Byte]): Array[Byte] = _decrypt(buffer, key)
    //    }
    //    def outgoing(outgoing: Rc4Key): Encrypt = new Encrypt {
    //
    //      private val key = init(outgoing.raw)
    //
    //      def encrypt(buffer: Array[Byte]): Array[Byte] = _encrypt(buffer, key)
    //    }
  }
  class Rc4Codec private[rc4] (incomming: Rc4Key, outgoing: Rc4Key) extends Codec {

    private val inKey = init(incomming.raw)
    private val outKey = init(outgoing.raw)

    encrypt(new Array[Byte](1024))
    decrypt(new Array[Byte](1024))

    def decrypt(buffer: Array[Byte]): Array[Byte] = _decrypt(buffer, inKey)
    def encrypt(buffer: Array[Byte]): Array[Byte] = _encrypt(buffer, outKey)
  }

  private def init(key: Array[Byte]): Array[Byte] = {
    val S = Array.ofDim[Byte](256)
    for (i <- 0 until 256) {
      S(i) = i.asInstanceOf[Byte];
    }
    var j = 0;
    for (i <- 0 until 256) {
      j = (j + S(i) + T(i, key)) & 0xFF
      swap(j, i, S)
    }
    key
  }
  private def T(index: Int, key: Array[Byte]) = key(index % key.length)

  private def swap(j: Int, i: Int, S: Array[Byte]) {
    // S[i] ^= S[j];
    // S[j] ^= S[i];
    // S[i] ^= S[j];
    val tmp = S(j)
    S(j) = S(i)
    S(i) = tmp
  }

  private[rc4] def _encrypt(plaintext: Array[Byte], S: Array[Byte]): Array[Byte] = {
    val ciphertext = Array.ofDim[Byte](plaintext.length)
    var i = 0
    var j = 0
    var k = 0
    var t = 0
    for (counter <- 0 until plaintext.length) {
      i = (i + 1) & 0xFF
      j = (j + S(i)) & 0xFF
      swap(j, i, S)
      t = (S(i) + S(j)) & 0xFF
      k = S(t)
      ciphertext(counter) = (plaintext(counter) ^ k).asInstanceOf[Byte]
    }
    ciphertext
  }

  private[rc4] def _decrypt(buffer: Array[Byte], key: Array[Byte]): Array[Byte] = _encrypt(buffer, key)

  trait Encrypt {
    def encrypt(msg: String): Array[Byte] = encrypt(msg.getBytes(UTF8))
    def encrypt(buffer: ByteBuffer): Array[Byte] = {
      val raw = new Array[Byte](buffer.remaining())
      buffer.get(raw)
      encrypt(raw)
    }
    def encrypt(buffer: Array[Byte]): Array[Byte]
  }

  trait Decrypt {
    def xdecrypt(buffer: Array[Byte]): String = new String(decrypt(buffer), UTF8)
    def decrypt(buffer: Array[Byte]): Array[Byte]
    def xdecrypt(buffer: ByteBuffer): String = new String(decrypt(buffer), UTF8)
    def decrypt(buffer: ByteBuffer): Array[Byte] = {
      val raw = new Array[Byte](buffer.remaining())
      buffer.get(raw)
      decrypt(raw)
    }
  }
  trait Codec extends Encrypt with Decrypt
}


