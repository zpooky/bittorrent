package com.spooky.bittorrent.l.session.client

import com.spooky.bittorrent.protocol.client.pwp.api.Choke
import com.spooky.bittorrent.protocol.client.pwp.api.Unchoke
import com.spooky.bittorrent.Showable
import com.spooky.bittorrent.model.PeerId
import com.spooky.cipher.MSEKeyPair

class ClientSession(val peerId: PeerId, val keyPair: MSEKeyPair) extends ClientViewableSession {
  var choking: Boolean = false
  def choked: Boolean = false
  private var i = 1

  def choke()(implicit write: Showable => Unit): Unit = {
    if (!choking) {
      println(i + ".choke")
      i = i + 1
//      connection ! Choke
      write(Choke)
      choking = true
    }
  }

  def unchoke()(implicit write: Showable => Unit): Unit = {
    if (choking) {
      println(i + ".unchoke")
      i = i + 1
//      connection ! Unchoke
      write(Unchoke)
      choking = false
    }
  }
}
