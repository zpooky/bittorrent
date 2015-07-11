package com.spooky.bittorrent.actors

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.actor.PeerWireProtocolsActors
import com.spooky.dht.DHTMessageActor
import com.spooky.dht.DHTServer
import com.spooky.dht.DHTHandlerProps
import com.spooky.bittorrent.BTActors
import com.spooky.dht.DHTActors

class BittorrentActors(val actorSystem: ActorSystem) extends BTActors with PeerWireProtocolsActors with DHTActors {
  def announce: ActorRef = actorSystem.actorOf(Props[AnnounceActor]())

  def start: ActorRef = actorSystem.actorOf(Props(classOf[TorrentStartActor]))

  def register(actor: ActorSystem => ActorRef): ActorRef = actor(actorSystem)
}

object TestDHT extends DHTHandlerProps {
  def props(connection: ActorRef) = Props(classOf[DHTMessageActor], connection)
}
