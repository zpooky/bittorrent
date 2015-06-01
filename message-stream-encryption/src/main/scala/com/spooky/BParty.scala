package com.spooky

import java.nio.charset.Charset
import com.spooky.rc4.Codec
import java.nio.Buffer
import java.nio.ByteBuffer
import org.apache.commons.codec.binary.Hex
import java.util.Arrays
import java.security.MessageDigest
import java.nio.ByteOrder
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.LocalSecret
import com.spooky.bittorrent.PublicKey
import com.spooky.bittorrent.SharedSecretKey
import com.spooky.bittorrent.BRc4Key
import com.spooky.bittorrent.ARc4Key
import com.spooky.bittorrent.Sha1
import com.spooky.bittorrent.Padding
import com.spooky.bittorrent.Base

//1 A->B: Diffie Hellman Ya, PadA
//2 B->A: Diffie Hellman Yb, PadB
//3 A->B: HASH('req1', S), HASH('req2', SKEY) xor HASH('req3', S), ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)), ENCRYPT(IA)
//4 B->A: ENCRYPT(VC, crypto_select, len(padD), padD), ENCRYPT2(Payload Stream)
//5 A->B: ENCRYPT2(Payload Stream)

class BParty(skey: InfoHash) extends Base with Party {
  println("b-skey: " + skey)
  private val localSecret = LocalSecret.spooky
  println("b-localSecret: " + localSecret)
  private val bPublicKey = PublicKey.generate(localSecret)
  println("b-publicKey: " + bPublicKey)
  private var sharedKey: SharedSecretKey = null
  //  private var rc4Key: Rc4Key = null
  private var rc4: Codec = null

  def receivePublic(publicKey: PublicKey): Unit = {
    sharedKey = SharedSecretKey(localSecret, publicKey)
    println("b-sharedKey: " + sharedKey)
    //    rc4Key = ARc4Key(sharedKey, skey)
    //    println("b-rc4Key: "+rc4Key)
    rc4 = Rc4Stock(BRc4Key(sharedKey, skey), ARc4Key(sharedKey, skey))
  }
  def sendPublic: PublicKey = bPublicKey

  //3 A->B: HASH('req1', S), HASH('req2', SKEY) xor HASH('req3', S), ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)), ENCRYPT(IA)
  //verification_constant = 8 bytes set to 0x00
  //crypto_provide = 0x01 means plaintext, 0x02 means RC4.  32bit
  //len(X) specifies the length of X in 2 bytes.
  //PadD: Arbitrary data with a length of 0 to 512 bytes
  //IA = initial payload data from A may be 0-sized
  def _3(r: Array[Byte]): Unit = {
    val buffer = ByteBuffer.wrap(r).order(ByteOrder.BIG_ENDIAN)
    val req1 = Sha1.raw(buffer)
//        assert(req1 == Sha1.from("req1", sharedKey))
    val obfuscated = Array.ofDim[Byte](20)
    buffer.get(obfuscated)
    val req2 = Sha1.from("req2", skey)
    val req3 = Sha1.from("req3", sharedKey)
//        assert(MessageDigest.isEqual(obfuscated, req2.xor(req3)))
    val decrypted = ByteBuffer.wrap(rc4.decrypt(buffer)).order(ByteOrder.BIG_ENDIAN)
        println("verification_constant:"+decrypted.getLong)
    println("crypto_provide:"+decrypted.getInt)
//    println(decrypted.getLong)
    val paddingL = decrypted.getShort
    println(buffer)
    println("paddingL:"+paddingL)
    buffer.position(buffer.position()+paddingL)
    println("remaining:"+buffer)
    //    assert(decrypted.getInt == 2)
    //    val paddingLength  = decrypted.getShort
    //    println(paddingLength)
    //    println(decrypted)
    //    println(new String(decrypted, UTF8))
    println()
  }
  //4 B->A: ENCRYPT(VC, crypto_select, len(padD), padD), ENCRYPT2(Payload Stream)
  //verification_constant = 8 bytes set to 0x00.
  //crypto_provide and crypto_select are a 32bit bitfields. 0x01 means plaintext, 0x02 means RC4
  //len(X) specifies the length of X in 2 bytes.
  def _4: Array[Byte] = encrypt(0, 2, Padding.random)

  private def encrypt(verificationConstant: Long, cryptoSelect: Int, padding: Padding): Array[Byte] = {
    val buffer = ByteBuffer.allocate(8 + 4 + 2 + padding.raw.length).order(ByteOrder.BIG_ENDIAN)
    buffer.putLong(verificationConstant)
    println("sending-vc:" + verificationConstant)
    buffer.putInt(cryptoSelect)
    println("sending-crypto_select:" + cryptoSelect)
    buffer.putShort(padding.raw.length.asInstanceOf[Short])
    println("sending-padL:" + padding.raw.length)
    buffer.put(padding.raw)
    rc4.encrypt(buffer.array())
  }

  def receive(chiperText: Array[Byte]): Unit = {
    println(new String(Hex.encodeHex(chiperText)) + "|" + new String(rc4.decrypt(chiperText), UTF8))
    println("||" + new String(rc4.decrypt(rc4.decrypt(chiperText)), UTF8))
    println("|||" + new String(rc4.decrypt(rc4.decrypt(rc4.decrypt(chiperText))), UTF8))
  }

  def send: Array[Byte] = rc4.encrypt("whetever!")
}
