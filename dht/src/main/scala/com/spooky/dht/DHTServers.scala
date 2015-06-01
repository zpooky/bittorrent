package com.spooky.dht

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.io.IO
import akka.io.Udp
import com.spooky.bittorrent.Config

class DHTServer(handlerProps: DHTHandlerProps) extends Actor {
  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(Config.hostname, Config.dhtPort))

  def receive = {
    case Udp.CommandFailed(_: Udp.Bind) => {
      context stop self
    }
    case Udp.SimpleSenderReady =>
      val handler = context.actorOf(handlerProps.props(sender))
      context.become(ready(handler))
  }

  def ready(send: ActorRef): Receive = {
    case r =>
      send ! r
  }
}
trait DHTHandlerProps {
  def props(connection: ActorRef): Props
}
