package com.spooky.bittorrent.actors

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props

class BittorrentActors(val actorSystem: ActorSystem) extends PeerWireProtocolsActors(actorSystem) {
  def announce: ActorRef = actorSystem.actorOf(Props[AnnounceActor]())
}

abstract class PeerWireProtocolsActors(actorSystem: ActorSystem) {
  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  private val api: ActorRef = actorSystem.actorOf(Props(classOf[PeerWireProtocolServer],test))
}