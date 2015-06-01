package com.spooky.bittorrent.mse

import scala.util.Random
import com.spooky.bittorrent.RawWrapper

sealed case class Padding(override val raw: Array[Byte]) extends RawWrapper(raw)
object Padding {
  def random: Padding = {
    val length = Random.nextInt(512)
    val padding = new Array[Byte](length)
    Padding(padding)
  }
}
//
