package com.spooky.bittorrent.mse

import java.nio.ByteBuffer
import com.spooky.bittorrent.RawWrapper

sealed case class PublicKey(override val raw: Array[Byte]) extends RawWrapper(raw)
  object PublicKey extends Base {

    //    def apply(sha1: Sha1): PublicKey = {
  //        @tailrec
  //        def rec(result: Array[Byte], raw: Array[Byte], prime: Array[Int], index: Int = 0): Array[Byte] = index match {
  //          case n if n == index => result
  //          case i => {
  //            //            result(i) = (2 ^  raw(i))
  //            rec(result, raw, prime, index + 1)
  //          }
  //        }
  //      val paddedRaw = Array.fill[Byte](96)(0)
  //      System.arraycopy(sha1.raw, 0, paddedRaw, 0, sha1.raw.length)
  //      val result = Array.fill[Byte](96)(0)
  //      PublicKey(rec(result, sha1.raw, dh_prime))
  //    }
  //Generator G is "2"
  //    (G^Xa) mod P
  def generate(secret: LocalSecret): PublicKey = {
    //        @tailrec
    //        def rec(result: Array[Byte], prime: Array[Int], index: Int = 0): Array[Byte] = index match {
    //          case n if n == result.length => result
    //          case i => {
    //            val t = (2 ^ result(i))
    //            val r = if (prime(i) == 0) t else t % prime(i)
    //            result(i) = (r).asInstanceOf[Byte]
    //            rec(result, prime, index + 1)
    //          }
    //        }
    //      println()
    //      Random.nextBytes(secret)
    //      PublicKey(rec(secret, dh_prime))
    //73706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B7973706F6F6B79
    //      val p = new java.math.BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563", 16)
    //      println("G = 2")
    //      println("P = "+Hex.encodeHexString(p.toByteArray()))
    //      println("(G^Xa) mod P")
    val xa = new java.math.BigInteger(secret.raw)
    //      println("Xa = " + Hex.encodeHexString(xa.toByteArray()))
    val g = java.math.BigInteger.valueOf(2)

    //      println("duplicate: " + Hex.encodeHexString(xa.pow(2).mod(p).toByteArray()))
    //(G^Xa) mod P
    PublicKey(g.modPow(xa, p).toByteArray())
  }

  def raw(buffer: ByteBuffer): PublicKey = {
    assert(buffer.remaining() >= 96)
    println("PublicKey.raw("+buffer+")")
    val raw = new Array[Byte](96)
    buffer.get(raw)
    PublicKey(raw)
  }
}
