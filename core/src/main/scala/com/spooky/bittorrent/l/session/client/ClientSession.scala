package com.spooky.bittorrent.l.session.client

import akka.actor.ActorRef
import com.spooky.bittorrent.protocol.client.pwp.api.Choke
import com.spooky.bittorrent.protocol.client.pwp.api.Unchoke

class ClientSession(connection: ActorRef) extends ClientViewableSession {
  var choking: Boolean = false
  def choked: Boolean = false
  private var i = 1

  def choke(): Unit = {
    if (!choking) {
      println(i + ".choke")
      i = i + 1
      connection ! Choke
      choking = true
    }
  }

  def unchoke(): Unit = {
    if (choking) {
      println(i + ".unchoke")
      i = i + 1
      connection ! Unchoke
      choking = false
    }
  }
}
