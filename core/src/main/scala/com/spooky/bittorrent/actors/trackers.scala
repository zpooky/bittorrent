package com.spooky.bittorrent.actors

import spooky.actor.Actor
import com.spooky.bittorrent.metainfo.Tracker
import spooky.actor.Props

object AnnounceActor {
  def props: Props = Props(classOf[AnnounceActor])
}
class AnnounceActor extends Actor {
  def receive = {
    //    case Announcing(tracker: Tracker) =>
    case o => println("received: " + o)
  }
}
