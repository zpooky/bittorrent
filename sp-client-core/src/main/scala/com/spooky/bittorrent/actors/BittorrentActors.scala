package com.spooky.bittorrent.actors

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.actor.PeerWireProtocolsActors
import com.spooky.dht.DHTMessageActor
import com.spooky.dht.DHTServer
import com.spooky.dht.DHTHandlerProps

class BittorrentActors(val actorSystem: ActorSystem) extends PeerWireProtocolsActors(actorSystem) {
  def announce: ActorRef = actorSystem.actorOf(Props[AnnounceActor]())

  val dhtAPI: ActorRef = actorSystem.actorOf(Props(classOf[DHTServer], TestDHT))

  def start: ActorRef = actorSystem.actorOf(Props(classOf[TorrentStartActor]))

}

object TestDHT extends DHTHandlerProps {
  def props(connection: ActorRef) = Props(classOf[DHTMessageActor], connection)
}
