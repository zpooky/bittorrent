package com.spooky.inbound.step

import com.spooky.inbound.InStep
import com.spooky.inbound.OutStep
import com.spooky.inbound.Base
import com.spooky.inbound.Reply
import spooky.util.ByteString
import com.spooky.bittorrent.InfoHash
import com.spooky.inbound.LocalPublicKey
import com.spooky.inbound.RemotePublicKey
import com.spooky.inbound.SecretKey
import java.nio.ByteBuffer
import java.security.MessageDigest
import com.spooky.bittorrent.Sha1
import javax.crypto.spec.SecretKeySpec
import com.spooky.bittorrent.data._
import com.spooky.cipher.ReadCipher
import java.util.Arrays
import akka.util.FakeBStrings
import java.nio.ByteOrder
import org.apache.commons.codec.binary.Hex
import com.spooky.inbound.CryptoProviders
import com.spooky.inbound.CryptoProvider
import com.spooky.cipher.RC4WriteCipher
import com.spooky.cipher.RC4ReadCipher

class ReceiveInfoStep(infoHashes: List[InfoHash], publicKey: LocalPublicKey, remotePublicKey: RemotePublicKey, secretKey: SecretKey) extends Base with InStep {
  def step(in: ByteString): ConfirmStep = {
    // B receives: HASH('req1', S), HASH('req2', SKEY)^HASH('req3', S),
    // ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
    // ENCRYPT(IA)

    val readBuffer = in.toByteBuffer.order(ByteOrder.BIG_ENDIAN)
    println(s"readBuffer: $readBuffer")

    if (!compareSecret(readBuffer)) {
      throw new RuntimeException("Not matching secret")
    }
    println(s"readBuffer: $readBuffer")

    val skey: SKey = determineCorrectSKey(Sha1.raw(readBuffer)).orNull
    if (skey == null) {
      throw new RuntimeException("No matching SKEY(InfoHash)")
    }

    val aKey = a(secretKey, skey)
    val bKey = b(secretKey, skey)

    val readCipher: RC4ReadCipher = new RC4ReadCipher(aKey)
    val writeCipher: RC4WriteCipher = new RC4WriteCipher(bKey)

    val crypto = cryptoProvider(readBuffer, readCipher)

    val paddingLength = padding(readBuffer, readCipher)
    readCipher.ignore(readBuffer, paddingLength)

    println("readBuffer: " + readBuffer)

    val initialLength = dataLength(readBuffer, readCipher)
    println("initialLength: " + initialLength)
    val data = readData(readBuffer, initialLength, readCipher)

    //    if (data.isDefined) {
    //      reply.reply(data.get)
    //    }
    new ConfirmStep(readCipher, writeCipher, crypto, data)
  }

  private def determineCorrectSKey(other: Sha1): Option[SKey] = {
    val part1 = Sha1.from(REQ3_IV, secretKey.raw)
    //    val search = Sha1(other.xor(part1))
    //...HASH('req2', SKEY)^HASH('req3', S)
    //        println("SKEY: " + skey)
//    println("other: " + other)
//    println("1: " + part1)
//    println("3: " + Sha1(other.xor(part1)))
    //decode.xor(sha1)
    //    Sha1(decode.xor(sha1)).equals(Sha1(skey.raw))
    //    println(search)
    infoHashes.find(c => {
//      println(s"-$c====${Sha1(Sha1.from(REQ2_IV, c.raw).xor(part1))}")
      Sha1(Sha1.from(REQ2_IV, c.raw).xor(part1)).equals(other)
    })
  }

  private def compareSecret(readBuffer: ByteBuffer): Boolean = {
    val digest = Sha1.from(REQ1_IV, secretKey.raw)
    println(digest + "|" + digest.length)
    compare(readBuffer, digest)
  }

  protected def compare(readBuffer: ByteBuffer, digest: Sha1): Boolean = {
    val cmp = Array.ofDim[Byte](digest.length)
    do {
      readBuffer.get(cmp)
      //      println("cmp|" + Hex.encodeHexString(cmp))
      readBuffer.position(readBuffer.position() - 19)
    } while (!MessageDigest.isEqual(cmp, digest.raw) && readBuffer.position() <= readBuffer.limit() - 20)
    //TODO verify that match

    if (MessageDigest.isEqual(cmp, digest.raw)) {
      readBuffer.position(readBuffer.position() + 19)
      true
    } else false
  }

  protected def a(secretBytes: SecretKey, sharedSecret: SKey): SecretKeySpec = xx(KEYA_IV, secretBytes, sharedSecret)
  protected def b(secretBytes: SecretKey, sharedSecret: SKey): SecretKeySpec = xx(KEYB_IV, secretBytes, sharedSecret)

  private def xx(iv: Array[Byte], secretKey: SecretKey, sharedKey: SKey): SecretKeySpec = {
    val secretBytes = secretKey.raw;
    val sharedSecret = sharedKey.raw;

    val aKey = Sha1.from(iv, secretBytes, sharedSecret)

    new SecretKeySpec(aKey.raw, RC4_STREAM_ALG)
  }
  private def cryptoProvider(readBuffer: ByteBuffer, readCipher: RC4ReadCipher): CryptoProvider = {
    // ENCRYPT(VC, crypto_provide, len(PadC),
    val size = VC.length + 4
    val crypted = Array.ofDim[Byte](size)

    readBuffer.get(crypted)
    println("crypted cryptoProvider: " + debug(crypted))

    val plain = ByteBuffer.wrap(readCipher.update(crypted)).order(ByteOrder.BIG_ENDIAN)
    println("cryptoProvider: " + debug(plain))
    plain.getLong
    println("plain: " + plain)
    CryptoProviders.from(plain.getInt)
  }

  private def padding(readBuffer: ByteBuffer, readCipher: ReadCipher): Int = {
    // ENCRYPT(VC, crypto_provide, len(PadC),
    val crypted = Array.ofDim[Byte](2)
    readBuffer.get(crypted)

    val plain = ByteBuffer.wrap(readCipher.update(crypted))

    val padding = plain.getShort
    println("padding: " + padding);
    if (padding < 0) {
      throw new RuntimeException("Invlaid padding: " + padding);
    } else if (padding > PADDING_MAX) {
      throw new RuntimeException("Invalid padding '" + padding + "'")
    }
    padding
  }

  private def dataLength(readBuffer: ByteBuffer, readCipher: ReadCipher): Int = {
    val lengthArr = Array.ofDim[Byte](2)
    readBuffer.get(lengthArr)
    val decodedArr = readCipher.update(lengthArr)
    val iaLength = ByteBuffer.wrap(decodedArr).order(ByteOrder.BIG_ENDIAN).getShort() & 0xFFFF;

    if (iaLength > 65535 || iaLength < 0) {
      throw new RuntimeException("Invalid IA length '" + iaLength + "'");
    }
    iaLength
  }

  private def readData(readBuffer: ByteBuffer, initialLength: Int, readCipher: ReadCipher): Option[ByteString] = {
    if (initialLength > 0) {
      val initialPayload = Array.ofDim[Byte](initialLength)
      assert(readBuffer.get(initialPayload) == initialLength)

      Some(FakeBStrings(readCipher.update(initialPayload)))
    } else None
  }

  private def debug(plain: ByteBuffer): String = Arrays.toString(plain.array)
  private def debug(plain: Array[Byte]): String = Arrays.toString(plain)
}
