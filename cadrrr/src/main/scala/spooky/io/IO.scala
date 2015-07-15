package spooky.io

import spooky.actor.ActorRef
import spooky.actor.ActorSystem
import spooky.actor.Props

/**
 * @author spooky
 */
object IO {
  def apply(channel: Channel)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(channel.props(actorSystem))
  }
}
