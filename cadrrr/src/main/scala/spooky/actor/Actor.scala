package spooky.actor

import scala.collection.JavaConversions._
import spooky.io.Tcp

object Actor {
  private[actor] val noSender: ActorRef = null
}
trait Actor {

  def receive: Receive

  type Receive = PartialFunction[Any, Unit]

  implicit lazy val context: ActorContext = new ActorContext(ActorSystem.singleton, ActorContext.getSelf) //TODO
  //is def since context is lazy
  final def sender(): ActorRef = context.sender()
  final implicit lazy val self: ActorRef = context.self

  def unhandled(message: Any): Unit = {
    message match {
      case Terminated(dead)        => throw new DeathPactException(dead)
      case _: Tcp.ConnectionClosed =>
      case r                       => println(s"in ${getClass}: unhandled class ${r.getClass} | message $r")
    }
  }

}
