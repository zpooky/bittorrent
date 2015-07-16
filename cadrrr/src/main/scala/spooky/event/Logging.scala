package spooky.event

import spooky.actor.ActorSystem
import spooky.actor.Actor

/**
 * @author spooky
 */
object Logging {
  def apply(system: ActorSystem, actor: Actor) = new Logging(system, actor)
}

class Logging(system: ActorSystem, actor: Actor) {
  def error(msg: String): Unit = {
//    println(msg)
  }
  def debug(msg: String): Unit = {
//    println(msg)
  }
}
