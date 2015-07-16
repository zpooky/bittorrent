package com.spooky.bittorrent.actors

import spooky.actor.ActorSystem
import spooky.actor.ActorRef
import spooky.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.actor.PeerWireProtocolsActors
import com.spooky.bittorrent.BTActors
import com.spooky.dht.DHTActors

class BittorrentActors(val actorSystem: ActorSystem) extends BTActors with PeerWireProtocolsActors with DHTActors {
  def announce: ActorRef = actorSystem.actorOf(Props(classOf[AnnounceActor]))

  def start: ActorRef = actorSystem.actorOf(Props(classOf[TorrentStartActor]))

  def register(actor: ActorSystem => ActorRef): ActorRef = actor(actorSystem)
}

