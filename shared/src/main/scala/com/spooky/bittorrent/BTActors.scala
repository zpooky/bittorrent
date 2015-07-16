package com.spooky.bittorrent

import spooky.actor.ActorSystem
import spooky.actor.ActorRef

trait BTActors {
  def register(actor: ActorSystem => ActorRef): ActorRef
}
