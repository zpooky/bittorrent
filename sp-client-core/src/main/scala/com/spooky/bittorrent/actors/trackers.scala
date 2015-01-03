package com.spooky.bittorrent.actors

import akka.actor.Actor
import com.spooky.bittorrent.peer.tracker.Announcing
import com.spooky.bittorrent.metainfo.Tracker

class AnnounceActor extends Actor {
  def receive = {
//    case Announcing(tracker: Tracker) ⇒ 
    case o => println("received: "+o)
  }
}