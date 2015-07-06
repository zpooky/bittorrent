package com.spooky.bittorrent.protocol.client.pwp.actor

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer

abstract class PeerWireProtocolsActors(actorSystem: ActorSystem) {
  //  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  val api: ActorRef = actorSystem.actorOf(Props(classOf[TCPServer], PeerWireProtocolMessageDeserializerActor))
}
