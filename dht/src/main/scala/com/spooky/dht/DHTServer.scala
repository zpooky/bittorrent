package com.spooky.dht

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.io.IO
import akka.io.Udp
import com.spooky.bittorrent.Config
import akka.actor.actorRef2Scala
import com.spooky.Message

object DHTServer {
  def props: Props = Props(classOf[DHTServer])
}

class DHTServer extends Actor {
  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(Config.hostname, Config.dhtPort))

  lazy val handler = system.actorOf(Props(classOf[DHTMessageActor]))

  def receive = {
    case Udp.CommandFailed(_: Udp.Bind) => {
      context stop self
    }
    case Udp.SimpleSenderReady =>
    case Udp.Received(data, remote) => {
      handler ! Message(data, remote, sender())
    }
    case l @ Udp.Bound =>
    case Udp.Unbind    => sender ! Udp.Unbind
    case Udp.Unbound   => context.stop(self)
  }

  //  def ready(send: ActorRef): Receive = {
  //    case r =>
  //      send ! r
  //  }
}
trait DHTHandlerProps {
  def props(connection: ActorRef): Props
}
