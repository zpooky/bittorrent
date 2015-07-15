package spooky.io

import spooky.actor.Actor
import spooky.actor.ActorSystem
import spooky.actor.Props

abstract class Channel {
  def props(implicit actorSystem: ActorSystem): Props
}
