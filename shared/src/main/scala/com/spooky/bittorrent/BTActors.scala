package com.spooky.bittorrent

import akka.actor.ActorSystem
import akka.actor.ActorRef

trait BTActors {
  def register(actor: ActorSystem => ActorRef): ActorRef
}
