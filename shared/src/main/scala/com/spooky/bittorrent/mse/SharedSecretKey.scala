package com.spooky.bittorrent.mse

import org.apache.commons.codec.binary.Hex
import com.spooky.bittorrent.RawWrapper

sealed case class SharedSecretKey(override val raw: Array[Byte]) extends RawWrapper(raw)
object SharedSecretKey extends Base {
  //(Ya^Xb) mod P
def apply( /*local*/ ya: LocalSecret, /*remote*/ xb: PublicKey): SharedSecretKey = {
  //        @tailrec
  //        def rec(result: Array[Byte], a: Array[Byte], b: Array[Byte], prime: Array[Int], index: Int = 0): Array[Byte] = index match {
  //          case n if n == a.length => result
  //          case i => {
  //            result(i) = ((a(i) ^ b(i)) % prime(i)).asInstanceOf[Byte]
  //            rec(result, a, b, prime, index + 1)
  //          }
  //        }
  //      val result = Array.fill[Byte](96)(0)

  //      SecretKey(rec(result, a.raw, b.raw, dh_prime))
  //      println(xb.toInteger)
  println("------")
  println("remote|" + xb)
  println("secret|" + ya)
  println("prime|" + Hex.encodeHexString(p.toByteArray()))
    SharedSecretKey(xb.toInteger.modPow(ya.toInteger, p).toByteArray())
  }
}
