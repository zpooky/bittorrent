package com.spooky

import collection.JavaConversions._
import com.spooky.rc4._
import java.net.ServerSocket
import java.nio.channels.SocketChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress
import java.net.Inet4Address
import scala.util.Random
import scala.annotation.tailrec
import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset
import com.spooky.bittorrent.mse.LocalSecret
import com.spooky.bittorrent.mse.PublicKey
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.mse.SharedSecretKey

object Testx {
  def main(args: Array[String]): Unit = {
    println(PublicKey.generate(LocalSecret.random))
    println(SharedSecretKey(LocalSecret.random, PublicKey.generate(LocalSecret.random)))
    import resource._
    for {
      server <- managed(ServerSocketChannel.open().bind(new InetSocketAddress(26555)))
      channel <- managed(server.accept)
      buffer = ByteBuffer.allocate(1024*100).order(ByteOrder.BIG_ENDIAN)
      _ = println(buffer)
      _ = channel.read(buffer)
      b = new BParty(InfoHash.hex("597f6a218a58b0fe7880ba12466ccd89ca6c778f"))
      _ = b.receivePublic(PublicKey.raw(buffer.flip().asInstanceOf[ByteBuffer]))
      _ = channel.write(ByteBuffer.wrap(b.sendPublic.raw).order(ByteOrder.BIG_ENDIAN))
      _ = buffer.clear()
      _ = channel.read(buffer)
      _ = b._3(buffer.array())
      _ = channel.write(ByteBuffer.wrap(b._4).order(ByteOrder.BIG_ENDIAN))
      _ = buffer.clear()
      _ = channel.read(buffer)
      _ = debug(b, buffer)

      //      aPublicKey = getPublicKey(buffer)
      //      bPublicKey = sendPublicKey(channel, buffer)
      //      key = SharedSecretKey(LocalSecret.random, bPublicKey)
      //      _ = Thread.sleep(500)
      //      _ = channel.read(buffer)
      //      skey = InfoHash.hex("BCC57E09BE32C73666248E12203FB6C76EC247B9")
      //      decryptKey = ARc4Key(key, skey)
      //      encryptKey = BRc4Key(key, skey)
      //      _ = debug(buffer, Rc4Stock(decryptKey, encryptKey))
      //      rc4 = handshake(channel)
    } {

    }

    new BParty(InfoHash.hex("BCC57E09BE32C73666248E12203FB6C76EC247B9"))
  }
  //    BIGNUM* prime = 0;
  //    BIGNUM* secret = 0;
  //    BIGNUM* remote_key = 0;
  //    BN_CTX* ctx = 0;
  //    int size;
  //
  //    prime = BN_bin2bn(dh_prime, sizeof(dh_prime), 0);
  //    if (prime == 0) { ret = 1; goto get_out; }
  //    secret = BN_bin2bn((unsigned char*)m_dh_local_secret, sizeof(m_dh_local_secret), 0);
  //    if (secret == 0) { ret = 1; goto get_out; }
  //    remote_key = BN_bin2bn((unsigned char*)remote_pubkey, 96, 0);
  //    if (remote_key == 0) { ret = 1; goto get_out; }
  //
  //    ctx = BN_CTX_new();
  //    if (ctx == 0) { ret = 1; goto get_out; }
  //    BN_mod_exp(remote_key, remote_key, secret, prime, ctx);
  //    BN_CTX_free(ctx);
  //
  //    // remote_key is now the shared secret
  //    size = BN_num_bytes(remote_key);
  //    memset(m_dh_shared_secret, 0, sizeof(m_dh_shared_secret) - size);
  //    BN_bn2bin(remote_key, (unsigned char*)m_dh_shared_secret + sizeof(m_dh_shared_secret) - size);
  //  def secretKey(a: IncommingKey, b: OutgoingKey): Array[Byte] = {
  //    val dh_prime = Array[Int]( //
  //      //
  //      0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xC9, 0x0F, 0xDA, 0xA2, //
  //      0x21, 0x68, 0xC2, 0x34, 0xC4, 0xC6, 0x62, 0x8B, 0x80, 0xDC, 0x1C, 0xD1, //
  //      0x29, 0x02, 0x4E, 0x08, 0x8A, 0x67, 0xCC, 0x74, 0x02, 0x0B, 0xBE, 0xA6, //
  //      0x3B, 0x13, 0x9B, 0x22, 0x51, 0x4A, 0x08, 0x79, 0x8E, 0x34, 0x04, 0xDD, //
  //      0xEF, 0x95, 0x19, 0xB3, 0xCD, 0x3A, 0x43, 0x1B, 0x30, 0x2B, 0x0A, 0x6D, //
  //      0xF2, 0x5F, 0x14, 0x37, 0x4F, 0xE1, 0x35, 0x6D, 0x6D, 0x51, 0xC2, 0x45, //
  //      0xE4, 0x85, 0xB5, 0x76, 0x62, 0x5E, 0x7E, 0xC6, 0xF4, 0x4C, 0x42, 0xE9, //
  //      0xA6, 0x3A, 0x36, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x05, 0x63)
  //
  //      @tailrec
  //      def rec(result: Array[Byte], a: Array[Int], b: Array[Int], prime: Array[Int], index: Int = 0): Array[Byte] = index match {
  //        case n if n == a.length => result
  //        case i => {
  //          result(i) = (((a(i) ^ b(i)) % prime(i)) & 0xFF).asInstanceOf[Byte]
  //          rec(result, a, b, prime, index + 1)
  //        }
  //      }
  //    val secret = Array.ofDim[Byte](a.raw.length)
  //    //     for (int i = 0; i < a.length; ++i) {
  //    //     secret[i] = (a[i] ^ b[i]) & 0xFF;
  //    //     }
  //    rec(secret, convert(a.raw), convert(b.raw), dh_prime, 0)
  //  }
  private val UTF8 = Charset.forName("UTF8")
  def debug(b: BParty, buffer: ByteBuffer): Unit = {
    println(new String(buffer.array(), UTF8))
    b.receive(buffer.array())
  }
  def convert(b: Array[Byte]): Array[Int] = {
    null
  }

  def handshake(channel: SocketChannel): Codec = {
    null
  }

  def sendPublicKey(channel: SocketChannel, buffer: ByteBuffer): PublicKey = {
    val key = PublicKey.generate(LocalSecret.random)
    buffer.put(key.raw)
    println("1::" + buffer.position())
    putRandomBytes(buffer, 512 - 20)
    println("1::" + buffer.position())
    buffer.flip()
    channel.write(buffer)
    buffer.clear
    key
  }

  def getPublicKey(buffer: ByteBuffer): PublicKey = {
    buffer.flip()
    val key = PublicKey.raw(buffer)
    println(key)
    buffer.clear
    key
  }

  def putRandomBytes(buffer: ByteBuffer, max: Int): Unit = {
    val length = Math.abs((Random.nextInt % max) / 8)
    println("1junk::" + length)
    for (_ <- 0 until length) {
      buffer.putInt(Random.nextInt)
    }
  }

  def debug(buffer: ByteBuffer, rc4: Codec) = {
    buffer.flip()
    println("debug: " + buffer)
    buffer.position(40)
    println(rc4.xdecrypt(buffer))
    println(buffer.limit())
  }
}
