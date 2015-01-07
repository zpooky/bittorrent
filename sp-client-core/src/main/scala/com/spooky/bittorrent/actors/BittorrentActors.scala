package com.spooky.bittorrent.actors

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.actor.PeerWireProtocolsActors

class BittorrentActors(val actorSystem: ActorSystem) extends PeerWireProtocolsActors(actorSystem) {
  def announce: ActorRef = actorSystem.actorOf(Props[AnnounceActor]())
}
