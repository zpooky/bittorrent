package com.spooky.dht

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.io.IO
import akka.io.Udp

class DHTServer(handlerProps: HandlerProps) extends Actor {
  import context.system
  IO(Udp) ! Udp.SimpleSender

  def receive = {
    case Udp.SimpleSenderReady =>
//      val handler = context.actorOf(handlerProps.props(remote, sender))
      context.become(ready(sender()))
  }

  //  def ready(send: ActorRef): Receive = {
  //    case msg: String =>
  //      send ! Udp.Send(ByteString(msg), remote)
  //  }
}
trait HandlerProps {
  def props(client: InetSocketAddress, connection: ActorRef): Props
}
