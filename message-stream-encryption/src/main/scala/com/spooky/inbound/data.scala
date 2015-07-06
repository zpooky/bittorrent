package com.spooky.inbound

import java.math.BigInteger
import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset

case class LocalPublicKey(raw: Array[Byte]) {
  override def toString: String = Hex.encodeHexString(raw)
}
object LocalPublicKey {
  def parse(bigInteger: BigInteger, bytes: Int): LocalPublicKey = {
    val ss = bigInteger.toString(16).substring(0, bytes).toCharArray()
    //    val hex = Hex.decodeHex()
    val raw = if (ss.length * 2 == bytes) {
      Hex.decodeHex(ss)
    } else { //TODO system.copy
      val prefix = zeros(bytes - (ss.length * 2)) + new String(ss) //, Charset.forName("UTF8")
      //      val b = Array.ofDim[Byte](bytes)
      //      var a = 0
      //      for (i <- (b.length - hex.length).to(b.length)) {
      //        b(i) = hex(a)
      //        a = a + 1
      //      }
      val s = prefix.toString
      println(s"dec: $s")
      Hex.decodeHex(s.toArray)
    }
    LocalPublicKey(raw)
  }

  def zeros(arg: Int): String = {
    var b = ""
    for (i <- 0 until arg) {
      b = b + "0"
    }
    b
  }

}
object CryptoProviders {
  def from(provider: Int): CryptoProvider = provider match {
    case 1 => Plain
    case 2 => RC4
    case n => throw new RuntimeException(s"Unknown crypto provider $n")
  }
}
abstract sealed class CryptoProvider {
  override def toString = getClass.getSimpleName.replaceAllLiterally("$", "")
}
final object RC4 extends CryptoProvider
final object Plain extends CryptoProvider

object RemotePublicKey {
  def apply(publicKey: java.security.PublicKey): RemotePublicKey = RemotePublicKey(publicKey.getEncoded)
}
case class RemotePublicKey(raw: Array[Byte]) {
  override def toString: String = Hex.encodeHexString(raw)
}
case class SecretKey(raw: Array[Byte]) {
  override def toString: String = Hex.encodeHexString(raw)
}
