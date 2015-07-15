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
    //    println(s"new: ${props.c.getSimpleName}")
    Test.t
    val actorFactory = new ActorFactory(props)

    val queue = new LinkedBlockingQueue[Tuple2[ActorRef, Any]]
    Test.list.add((props.c.getSimpleName, queue))
    val deathPact = new CopyOnWriteArrayList[ActorRef]

    val actorRef = new ActorRef(queue, deathPact)

    executors.submit(new ActorThread(actorFactory, queue, deathPact, actorRef))
    actorRef
  }
}
//PeerWireProtocolMessageDeserializerActor: 1955506
object Test {
  val list = new CopyOnWriteArrayList[Tuple2[String, LinkedBlockingQueue[Tuple2[ActorRef, Any]]]]
  lazy val t = ActorSystem.singleton.executors.submit(runna)

  def runna: Runnable = new Runnable {
    def run(): Unit = {
      while (true) {
        for (c <- list) {
          if (!c._2.isEmpty()) {
            println(s"${c._1}: ${c._2.size()}")
          }
        }
        Thread.sleep(5000)
      }
    }
  }
}