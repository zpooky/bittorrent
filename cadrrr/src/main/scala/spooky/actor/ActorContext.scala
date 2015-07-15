package spooky.actor

import scala.collection.JavaConversions._

object ActorContext {
  private val self = new ThreadLocal[ActorRef]
  private[actor] def setSelf(self: ActorRef): Unit = {
    this.self.set(self)
  }
  private[actor] def getSelf: ActorRef = self.get
}

class ActorContext(actorSystm: ActorSystem, _self: ActorRef) {

  def stop(actorRef: ActorRef): Unit = Thread.currentThread().interrupt()

  def watch(actorRef: ActorRef): Unit = actorRef.registerDeathPack(_self)

  def actorOf(props: Props) = system.actorOf(props)

  implicit def system: ActorSystem = actorSystm

  private[actor] var _sender: ActorRef = null
  final def sender(): ActorRef = _sender

  final def self: ActorRef = _self

  private[actor] var receive: PartialFunction[Any, Unit] = null
  def become(receive: PartialFunction[Any, Unit]): Unit = this.receive = receive
}
