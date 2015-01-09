package com.spooky.bittorrent.protocol.client.pwp.actor

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Actor
import akka.io.Tcp.Received
import akka.io.Tcp.ConnectionClosed
import com.spooky.bittorrent.protocol.client.pwp.tcp.TCPServer
import com.spooky.bittorrent.protocol.client.pwp.api.Handshake
import com.spooky.bittorrent.protocol.client.pwp.api.PeerWireMessage
import akka.actor.PoisonPill
import com.spooky.bittorrent.protocol.client.pwp.tcp.HandlerProps

abstract class PeerWireProtocolsActors(actorSystem: ActorSystem) {
  //  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  val api: ActorRef = actorSystem.actorOf(Props(classOf[TCPServer], Test))
}
//pstrlen:19|pstr:BitTorrent protocol|reserved:0000000000000000000000000000000000000000000100000000000000000000|info_has:Yj!ﾊXﾰ￾xﾀﾺFlￍﾉￊlwﾏ|peer-id:-lt0D20-Pﾖ￷ﾒﾛﾜￋ￿s

class Test(connection: ActorRef) extends Actor {
  override def receive = {
    case Received(data) => {
      //      println(":" + data)
      val handshake = Handshake(data)
      //      println(handshake)
      //      println(PeerWireMessage(data))
    }
    case (m: ConnectionClosed) => self ! PoisonPill
    case r                     => println("xAxAx:" + r)
    //    case PeerClosed      =>
    //    case ErrorClosed     =>
    //    case Closed          =>
    //    case ConfirmedClosed =>
    //    case Aborted         =>
  }
}
object Test extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[Test], connection)
}

