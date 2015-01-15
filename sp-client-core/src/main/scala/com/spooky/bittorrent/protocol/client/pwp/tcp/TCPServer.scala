package com.spooky.bittorrent.protocol.client.pwp.tcp

import akka.actor.Actor
import akka.io.Tcp
import akka.io.IO
import java.net.InetSocketAddress
import com.spooky.bittorrent.Config
import akka.actor.ActorRef
import akka.io.Tcp._
import akka.actor.ActorSystem
import akka.actor.Props

class TCPServer(handlerProps: HandlerProps) extends Actor {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress(Config.hostname, Config.peerWireProtocolPort))

  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self

    case Tcp.Connected(remote, local) =>
      val handler = context.actorOf(handlerProps.props(remote, sender))
      sender ! Tcp.Register(handler)
  }

}

trait HandlerProps {
  def props(client: InetSocketAddress, connection: ActorRef): Props
}
