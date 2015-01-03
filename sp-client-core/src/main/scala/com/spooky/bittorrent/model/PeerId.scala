package com.spooky.bittorrent.model

case class PeerId(id:String)
object PeerId {
  def create = PeerId("SPOOKY6-c2b4f6c4h4d9")
}