package com.spooky.bittorrent.protocol.client.pwp.actor

import spooky.actor.ActorSystem
import spooky.actor.ActorRef
import spooky.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer
import com.spooky.bittorrent.actors.BittorrentActors
import com.spooky.bittorrent.BTActors

trait PeerWireProtocolsActors {
  this: BTActors =>
  val api: ActorRef = register(_.actorOf(Props(classOf[TCPServer], PeerWireProtocolMessageDeserializerActor)))
}
