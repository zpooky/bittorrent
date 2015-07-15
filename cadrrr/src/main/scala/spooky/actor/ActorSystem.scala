package spooky.actor

import scala.collection.JavaConversions._
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.CopyOnWriteArrayList
import scala.annotation.tailrec
import java.util.Arrays

object ActorSystem {
  private[actor] val singleton = new ActorSystem
  def apply(name: String): ActorSystem = singleton
}
class ActorSystem {
  val executors = Executors.newCachedThreadPool


  def actorOf(props: Props): ActorRef = {
    val actorFactory = new ActorFactory(props)

    val queue = new LinkedBlockingQueue[Tuple2[ActorRef, Any]]
    val deathPact = new CopyOnWriteArrayList[ActorRef]

    val actorRef = new ActorRef(queue, deathPact)

    executors.submit(new ActorThread(actorFactory, queue, deathPact, actorRef))
    actorRef
  }
}

