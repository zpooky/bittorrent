package com.spooky.bittorrent.protocol.client.pwp.actor

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Actor
import akka.io.Tcp.Received
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer

abstract class PeerWireProtocolsActors(actorSystem: ActorSystem) {
  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  val api: ActorRef = actorSystem.actorOf(Props(classOf[TCPServer], test))
}

class Test extends Actor {
  override def receive = {
    case Received(data) => {
      println("xxxccCCvvv:" + data)
      val builderChar = StringBuilder.newBuilder
      for (n <- 0 until data.length) {
        builderChar.append(n.asInstanceOf[Char])
      }

      println("||" + builderChar.toString + "===")
    }

    case r => println("xAxAx:" + r)
    //    case PeerClosed      =>
    //    case ErrorClosed     =>
    //    case Closed          =>
    //    case ConfirmedClosed =>
    //    case Aborted         =>
  }
}
