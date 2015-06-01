package com.spooky.bittorrent

import scala.util.Random

sealed case class Padding(override val raw: Array[Byte]) extends RawWrapper(raw)
object Padding {
  def random: Padding = {
    val length = Random.nextInt(512)
    val padding = new Array[Byte](length)
    Padding(padding)
  }
}
//
