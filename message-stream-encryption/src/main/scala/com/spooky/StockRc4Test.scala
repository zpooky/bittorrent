package com.spooky

import com.spooky.rc4.Codec
import com.spooky.rc4.Decrypt
import com.spooky.rc4.Encrypt
import org.bouncycastle.crypto.engines.RC4Engine
import org.bouncycastle.crypto.params.KeyParameter
import scala.util.Try
import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset
import java.security.MessageDigest
import com.spooky.rc4.Codec
import com.spooky.bittorrent.mse.Rc4Key

object Rc4Stock {

//init_pe_rc4_handler(secret, info_hash);
//
  //init_pe_rc4_handler(char const* secret, sha1_hash const& stream_key)
//      static const char keyA[] = "keyA";
//    static const char keyB[] = "keyB";
//
//    // encryption rc4 longkeys
//    // outgoing connection : hash ('keyA',S,SKEY)
//    // incoming connection : hash ('keyB',S,SKEY)
//
//    if (is_outgoing()) h.update(keyA, 4); else h.update(keyB, 4);
//    h.update(secret, dh_key_len);
//    h.update((char const*)stream_key.begin(), 20);
//    const sha1_hash local_key = h.final();
//
//    h.reset();
//
//    // decryption rc4 longkeys
//    // outgoing connection : hash ('keyB',S,SKEY)
//    // incoming connection : hash ('keyA',S,SKEY)
//
//    if (is_outgoing()) h.update(keyB, 4); else h.update(keyA, 4);
//    h.update(secret, dh_key_len);
//    h.update((char const*)stream_key.begin(), 20);
//    const sha1_hash remote_key = h.final();
//
//    TORRENT_ASSERT(!m_enc_handler.get());
//    m_enc_handler.reset(new (std::nothrow) rc4_handler);
//    m_enc_handler->set_incoming_key(&remote_key[0], 20);
//    m_enc_handler->set_outgoing_key(&local_key[0], 20);

  def apply(decryptKey: Rc4Key, encryptKey: Rc4Key): Codec = {
   new XX(decryptKey.raw, encryptKey.raw)
  }
  private val UTF8 = Charset.forName("UTF8")
  class XX(decryptRaw: Array[Byte], encryptRaw: Array[Byte]) extends Codec {

    private val decryptKey = init(decryptRaw)
    private val encryptKey = init(encryptRaw)

    encrypt(new Array[Byte](1024))
    decrypt(new Array[Byte](1024))

    def decrypt(buffer: Array[Byte]): Array[Byte] = _encrypt(buffer, decryptKey)
    def encrypt(buffer: Array[Byte]): Array[Byte] = _encrypt(buffer, encryptKey)
    def init(key: Array[Byte]): RC4Engine = {
      val rc4Engine = new RC4Engine()
      val keyParam = new KeyParameter(key)
      rc4Engine.init(true, keyParam)
      rc4Engine
    }
  }

  private def _encrypt(plaintext: Array[Byte], engine: RC4Engine): Array[Byte] = {
    val out = new Array[Byte](plaintext.length);
//    val copy = new Array[Byte](plaintext.length);
//    System.arraycopy(plaintext, 0, copy, 0, plaintext.length)
    engine.processBytes(plaintext, 0, plaintext.length, out, 0);
    out
  }
  //  def main(args: Array[String]): Unit = {
  //    val test1_key = MessageDigest.getInstance("sha1").digest("spooky".getBytes(Charset.forName("UTF8")))
  //    val test2_key = MessageDigest.getInstance("sha1").digest("ykoops".getBytes(Charset.forName("UTF8")))
  //    val _1to2 = Rc4Stock(test2_key, test1_key)
  //    val _2to1 = Rc4Stock(test1_key, test2_key)
  //    Try {
  //      val secret = _1to2.xencrypt("secret".getBytes)
  //      println(Hex.encodeHexString(secret))
  //      println(Hex.encodeHexString(_1to2.xdecrypt(secret)))
  //      println(Hex.encodeHexString(_1to2.xencrypt(secret)))
  //      println(_2to1.decrypt(secret) + "|" + Hex.encodeHexString(secret))
  //    }
  //    println("----")
  //    Try {
  //      val secret = _2to1.xencrypt("secret".getBytes)
  //      println(Hex.encodeHexString(secret))
  //      println(Hex.encodeHexString(_2to1.xdecrypt(secret)))
  //      println(Hex.encodeHexString(_2to1.xencrypt(secret)))
  //      println(_1to2.decrypt(secret) + "|" + Hex.encodeHexString(secret))
  //    }
  //  }
}
