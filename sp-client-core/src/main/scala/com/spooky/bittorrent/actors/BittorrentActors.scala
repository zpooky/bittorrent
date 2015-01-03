package com.spooky.bittorrent.actors

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props

class BittorrentActors(actorSystem: ActorSystem) {
  def announce:ActorRef = actorSystem.actorOf(Props[AnnounceActor]())
}