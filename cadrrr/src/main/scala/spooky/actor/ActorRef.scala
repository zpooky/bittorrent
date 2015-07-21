package spooky.actor

import scala.collection.JavaConversions._
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.CopyOnWriteArraySet

class ActorRef private[actor] (private val queue: LinkedBlockingQueue[Tuple2[ActorRef, Any]], private val deathPact: CopyOnWriteArraySet[ActorRef]) {

  private[actor] def registerDeathPack(actorRef: ActorRef): Unit = deathPact.add(actorRef)

  def !(msg: Any)(implicit sender: ActorRef = Actor.noSender): Unit = {
//    if(sender == null){
//      println("Sender is null")
//      Thread.dumpStack()
//    }
    assert(queue.offer((sender, msg)))
  }
}
