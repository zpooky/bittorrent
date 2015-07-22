package spooky.actor

import scala.collection.JavaConversions._
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.CopyOnWriteArraySet
import spooky.io.Tcp

private class ActorThread(private val actorFactory: ActorFactory, private val queue: LinkedBlockingQueue[Tuple2[ActorRef, Any]], private val deathPact: CopyOnWriteArraySet[ActorRef], private implicit val self: ActorRef) extends Runnable {
  def run(): Unit = {
    ActorContext.setSelf(self)
    val actor = actorFactory.create
    actor.context.become(actor.receive)
    try {
      while (!Thread.interrupted()) {
        val entry = queue.take()

        val sender = entry._1
        actor.context._sender = sender

        val value = entry._2

        val receiver = actor.context.receive
        if (receiver.isDefinedAt(value)) {
          receiver(entry._2)
        } else actor.unhandled(value)
      }
      println("interrupted")
    } catch {
      case e: DeathPactException =>
      case e: Throwable          => e.printStackTrace()
    } finally {
      terminate(deathPact)
      ActorContext.setSelf(null)
    }
  }

  private def terminate(deathPact: CopyOnWriteArraySet[ActorRef]): Unit = {
    for (c <- deathPact) {
      c ! Terminated(self)
    }
    deathPact.clear()
  }
}
