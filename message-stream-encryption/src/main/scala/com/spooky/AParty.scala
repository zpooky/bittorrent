package com.spooky

import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.LocalSecret
import com.spooky.bittorrent.PublicKey
import com.spooky.bittorrent.SharedSecretKey
import com.spooky.bittorrent.ARc4Key
import com.spooky.bittorrent.BRc4Key
import com.spooky.bittorrent.Sha1
import com.sun.crypto.provider.Padding
import com.spooky.bittorrent.Padding
import java.nio.ByteOrder
import java.nio.ByteBuffer
import com.spooky.bittorrent.Base
import com.spooky.rc4.Codec
import org.apache.commons.codec.binary.Hex

class AParty(private val skey: InfoHash) extends Base with Party {
  println("a-skey: " + skey)
  private val aLocalSecret = LocalSecret.ykoops
  println("a-localSecret: " + aLocalSecret)
  private val aPublicKey = PublicKey.generate(aLocalSecret)
  println("a-publicKey: " + aPublicKey)
  private var sharedKey: SharedSecretKey = null
  //    private var rc4Key: Rc4Key = null
  private var rc4: Codec = null

  //    private var bRc4Key: Rc4Key = null

  def receivePublic(publicKey: PublicKey): Unit = {
    sharedKey = SharedSecretKey(aLocalSecret, publicKey)
    println("a-sharedKey: " + sharedKey)
    //      rc4Key = ARc4Key(sharedKey, skey)
    //      println("a-rc4Key: " + rc4Key)
    rc4 = Rc4Stock(ARc4Key(sharedKey, skey), BRc4Key(sharedKey, skey))
  }
  def sendPublic: PublicKey = aPublicKey

  //3 A->B: HASH('req1', S), HASH('req2', SKEY) xor HASH('req3', S), ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)), ENCRYPT(IA)
  //verification_constant = 8 bytes set to 0x00
  //crypto_provide = 0x01 means plaintext, 0x02 means RC4.  32bit
  //len(X) specifies the length of X in 2 bytes.
  //PadD: Arbitrary data with a length of 0 to 512 bytes
  //IA = initial payload data from A may be 0-sized
  def _3: Array[Byte] = {
    val req1 = Sha1.from("req1", sharedKey)
    val req2 = Sha1.from("req2", skey)
    val req3 = Sha1.from("req3", sharedKey)
    val obfscHash = req2.xor(req3)
    val padding = Padding.random
    val discrad = Padding.random.raw
    arr(req1, obfscHash, encrypt(0, 2, padding, discrad.length.asInstanceOf[Short]), encrypt(discrad))
  }

  private def encrypt(verificationConstant: Long, cryptoProvide: Int, padding: Padding, iaLength: Short): Array[Byte] = {
    val buffer = ByteBuffer.allocate(8 + 4 + 2 + padding.raw.length + 2).order(ByteOrder.BIG_ENDIAN)
    buffer.putLong(verificationConstant)
    buffer.putInt(cryptoProvide)
    buffer.putShort(padding.raw.length.asInstanceOf[Short])
    buffer.put(padding.raw)
    buffer.putShort(iaLength)
    rc4.encrypt(buffer)
  }
  private def encrypt(data: Array[Byte]): Array[Byte] = {
    rc4.encrypt(data)
  }

  private def arr(req1: Sha1, obfscHash: Array[Byte], encrypted: Array[Byte], ia: Array[Byte]): Array[Byte] = {
    val buffer = ByteBuffer.allocate(20 + obfscHash.length + encrypted.length + ia.length)
    buffer.put(req1.raw)
    buffer.put(obfscHash)
    buffer.put(encrypted)
    buffer.put(ia)
    buffer.array()
  }

  //4 B->A: ENCRYPT(VC, crypto_select, len(padD), padD), ENCRYPT2(Payload Stream)
  //verification_constant = 8 bytes set to 0x00.
  //crypto_provide and crypto_select are a 32bit bitfields. 0x01 means plaintext, 0x02 means RC4
  //len(X) specifies the length of X in 2 bytes.
  def _4(r: ByteBuffer): Unit = {
    println(r)
    val b = ByteBuffer.wrap(rc4.decrypt(r))
    val vc = b.getLong
    println("vc:"+vc)
    val crypto_select = b.getInt
    println("crypto_select:"+crypto_select)
    val paddL = b.getShort
    println("padL:"+paddL)
    b.position(b.position()+paddL)
    println("remaining:"+b)
  }

  def receive(chiperText: Array[Byte]): Unit = {
    println(new String(Hex.encodeHex(chiperText)) + "|" + new String(rc4.decrypt(chiperText), UTF8))
  }

  def send: Array[Byte] = rc4.encrypt("this is a secret")

}
