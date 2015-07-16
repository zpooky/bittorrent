package com.spooky.bittorrent.protocol.client.pwp.tcp

import spooky.actor.Actor
import spooky.io.Tcp
import spooky.io.IO
import java.net.InetSocketAddress
import com.spooky.bittorrent.Config
import spooky.actor.ActorRef
import spooky.actor.ActorSystem
import spooky.actor.Props

class TCPServer(handlerProps: HandlerProps) extends Actor {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress(Config.hostname, Config.peerWireProtocolPort))

  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) => {
      context stop self
    }

    case Tcp.Connected(remote, local) => {
      val handler = context.actorOf(handlerProps.props(remote, sender()))
      sender ! Tcp.Register(handler, remote, keepOpenOnPeerClosed = true, useResumeWriting = false)
    }
  }

}

trait HandlerProps {
  def props(client: Tcp.Address, connection: ActorRef): Props
}
