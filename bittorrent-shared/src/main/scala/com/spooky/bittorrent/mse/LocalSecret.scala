package com.spooky.bittorrent.mse

import scala.util.Random
import com.spooky.bittorrent.RawWrapper

sealed case class LocalSecret(override val raw: Array[Byte]) extends RawWrapper(raw)
object LocalSecret {
  def spooky: LocalSecret = {
    val secretRaw = Array.ofDim[Byte](96)
    val spooky = "spooky"
    for (i <- 0 until 96) {
      secretRaw(i) = spooky.charAt(i % spooky.length()).asInstanceOf[Byte]
      //        print(spooky.charAt(i % spooky.length()))
    }
    LocalSecret(secretRaw)
  }
  def ykoops: LocalSecret = {
    val secretRaw = Array.ofDim[Byte](96)
    val ykoops = "ykoops"
    for (i <- 0 until 96) {
      secretRaw(i) = ykoops.charAt(i % ykoops.length()).asInstanceOf[Byte]
      //        print(spooky.charAt(i % spooky.length()))
    }
    LocalSecret(secretRaw)
  }
  def random: LocalSecret = {
    val secretRaw = Array.ofDim[Byte](96)
    Random.nextBytes(secretRaw)
    LocalSecret(secretRaw)
  }
}
