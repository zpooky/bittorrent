package com.spooky.inbound.step

import com.spooky.inbound.InStep
import com.spooky.inbound.OutStep
import com.spooky.inbound.Base
import com.spooky.inbound.Reply
import akka.util.ByteString
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
import com.spooky.cipher.WriteRC4Cipher
import com.spooky.cipher.ReadRC4Cipher

class ReceiveInfoStep(skey: InfoHash, publicKey: LocalPublicKey, remotePublicKey: RemotePublicKey, secretKey: SecretKey) extends Base with InStep {
  def step(in: ByteString, reply: Reply): ConfirmStep = {
    // B receives: HASH('req1', S), HASH('req2', SKEY)^HASH('req3', S),
    // ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
    // ENCRYPT(IA)

    //itterate over skeys (2) to find the correct

    val readBuffer = in.toByteBuffer.order(ByteOrder.BIG_ENDIAN)
    println(readBuffer)

    if (!compareSecret(readBuffer)) {
      println(readBuffer)
      throw new RuntimeException("Not matching secret")
    }

    sharedSecret(Sha1.raw(readBuffer))

    val aKey = a(secretKey, skey);
    val bKey = b(secretKey, skey);

    val readCipher: ReadRC4Cipher = new ReadRC4Cipher(aKey)
    val writeCipher: WriteRC4Cipher = new WriteRC4Cipher(bKey)

    val crypto = cryptoProvider(readBuffer, readCipher)

    val paddingLength = padding(readBuffer, readCipher)
    readCipher.ignore(readBuffer, paddingLength)

    println("readBuffer: " + readBuffer)

    val initialLength = dataLength(readBuffer, readCipher)
    println("initialLength: " + initialLength)
    val data = readData(readBuffer, initialLength, readCipher)

    if (data.isDefined) {
      reply.reply(data.get)
    }
    new ConfirmStep(readCipher, writeCipher, crypto)
  }

  private def sharedSecret(decode: Sha1): Unit = {
    val sha1 = Sha1.from(REQ3_IV, secretKey.raw)
    Sha1(decode.xor(sha1))
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
  private def cryptoProvider(readBuffer: ByteBuffer, readCipher: ReadCipher): CryptoProvider = {
    val size = VC.length + 4
    val crypted = Array.ofDim[Byte](size)

    readBuffer.get(crypted)

    val plain = ByteBuffer.wrap(readCipher.update(crypted))
    plain.position(plain.position() + VC.length)
    println(plain)
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
}
