package com.spooky

import com.spooky.rc4.Codec
import com.spooky.rc4.Decrypt
import com.spooky.rc4.Encrypt
import scala.util.Try
import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset
import java.security.MessageDigest
import com.spooky.rc4.Rc4
import com.spooky.bittorrent.Sha1

object Main {
  def main(args: Array[String]): Unit = {

//    test(Rc4.apply _)
    println("----------------------------------------")
//    test(Rc4Stock.apply _)
  }

  def test(arg: Function2[Sha1, Sha1, Codec]) = {
    val test1_key = Sha1.from("spooky");
    val test2_key = Sha1.from("ykoops");

    val _1to2 = arg(test2_key, test1_key)
    val _2to1 = arg(test1_key, test2_key)
    Try {
      val secret = _1to2.encrypt("secret")
      println(Hex.encodeHexString(secret))
      println(Hex.encodeHexString(_1to2.decrypt(secret)))
      println(Hex.encodeHexString(_1to2.encrypt(secret)))
      println(_2to1.xdecrypt(secret) + "|" + Hex.encodeHexString(secret))
    }
    println("----")
    Try {
      val secret = _2to1.encrypt("secret")
      println(Hex.encodeHexString(secret))
      println(Hex.encodeHexString(_2to1.decrypt(secret)))
      println(Hex.encodeHexString(_2to1.encrypt(secret)))
      println(_1to2.xdecrypt(secret) + "|" + Hex.encodeHexString(_1to2.decrypt(secret)))
    }
  }
}
