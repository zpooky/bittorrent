package com.spooky.bittorrent.actors

import akka.actor.Actor
import akka.io.Tcp
import akka.io.IO
import java.net.InetSocketAddress
import com.spooky.bittorrent.Config
import akka.actor.ActorRef
//import akka.io.Tcp.Received
import akka.io.Tcp._

class PeerWireProtocolServer(handler: ActorRef) extends Actor {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress(Config.hostname, Config.peerWireProtocolPort))

  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) ⇒ context stop self

    case Tcp.Connected(remote, local) ⇒
      sender ! Tcp.Register(handler)
  }

}

class Test extends Actor {
  override def receive = {
    case Received(data)  ⇒ println(data)
    case r => println(r)
//    case PeerClosed      ⇒
//    case ErrorClosed     ⇒
//    case Closed          ⇒
//    case ConfirmedClosed ⇒
//    case Aborted         ⇒
  }
}