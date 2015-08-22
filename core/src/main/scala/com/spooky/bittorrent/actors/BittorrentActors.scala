package com.spooky.bittorrent.actors

import spooky.actor.ActorSystem
import spooky.actor.ActorRef
import spooky.actor.Props
import com.spooky.bittorrent.protocol.client.pwp.actor.PeerWireProtocolsActors
import com.spooky.bittorrent.BTActors
import com.spooky.dht.DHTActors
import com.spooky.bittorrent.l.session.Sessions
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer
import com.spooky.bittorrent.protocol.client.pwp.actor.PeerWireProtocolMessageDeserializerActor

class BittorrentActors(val actorSystem: ActorSystem) extends BTActors with PeerWireProtocolsActors with DHTActors {
  val sessions: Sessions = new Sessions(actorSystem)

  lazy val start: ActorRef = actorSystem.actorOf(TorrentStartActor.props(this, sessions))

  def register(actor: ActorSystem => ActorRef): ActorRef = actor(actorSystem)

  val api: ActorRef = actorSystem.actorOf(Props(classOf[TCPServer], PeerWireProtocolMessageDeserializerActor.props(sessions)))
}

