package com.spooky.dht

import spooky.actor.ActorRef
import com.spooky.bittorrent.BTActors

trait DHTActors {
  this: BTActors =>

  val dhtAPI: ActorRef = register(_.actorOf(DHTServer.props))
}
