package com.spooky.bittorrent.protocol.client.pwp.actor

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer
import com.spooky.bittorrent.actors.BittorrentActors
import com.spooky.bittorrent.BTActors

trait PeerWireProtocolsActors {
  this: BTActors =>
  //  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  val api: ActorRef = register(_.actorOf(Props(classOf[TCPServer], PeerWireProtocolMessageDeserializerActor)))
}
