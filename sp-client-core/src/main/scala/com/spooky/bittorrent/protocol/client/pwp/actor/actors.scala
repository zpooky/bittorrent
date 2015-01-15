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
import java.net.InetSocketAddress
import java.nio.ByteOrder
import java.nio.ByteBuffer
import com.spooky.bittorrent.l.SessionManager
import akka.io.Tcp.Write
import com.spooky.bittorrent.protocol.client.pwp.api.Bitfield

abstract class PeerWireProtocolsActors(actorSystem: ActorSystem) {
  //  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  val api: ActorRef = actorSystem.actorOf(Props(classOf[TCPServer], Test))
}
//pstrlen:19|pstr:BitTorrent protocol|reserved:0000000000000000000000000000000000000000000100000000000000000000|info_has:Yj!ﾊXﾰ￾xﾀﾺFlￍﾉￊlwﾏ|peer-id:-lt0D20-Pﾖ￷ﾒﾛﾜￋ￿s

class Test(client: InetSocketAddress, connection: ActorRef) extends Actor {
  private var first = true
  private var session: SessionManager = null
  override def receive = {
    case Received(data) => {
      //      println(":" + data)
      val buffer = data.toByteBuffer
      buffer.order(ByteOrder.BIG_ENDIAN)
      println("====================")
      println(client.getHostString + "|" + first)
      if (first) {
        if (buffer.duplicate.get == 19) {
          val handshake = Handshake(buffer)
          first = false
          handle.isDefinedAt(handshake)
          println(handshake)
          //        println(PeerWireMessage(data.toByteBuffer))
        }
        //        handle.isDefinedAt(PeerWireMessage(data.toByteBuffer))
        //        buffer.position(buffer.capacity())
      }
      //      debug(buffer.duplicate)
      //      println("before_" + buffer)
      if (buffer.hasRemaining()) {
        handle.isDefinedAt(PeerWireMessage(buffer))
        //        println("after_" + buffer)
      }
      println("====================")
    }
    case (m: ConnectionClosed) => self ! PoisonPill
    case r                     => println("xAxAx:" + r)
  }
  def handle: PartialFunction[PeerWireMessage, Unit] = {
    case Handshake(infoHash, _) => {
      session = SessionManager.get(infoHash).get
      connection ! Write(Handshake(infoHash, session.peerId).toByteString)
      val fileManager = session.fileManager
      if (fileManager.haveAnyBlocks) {
        connection ! Write(Bitfield(fileManager.blocks).toByteString)//TODO implement toByteString
      }
    }
    case a => println("!!!!!" + a.getClass.getSimpleName)
  }

  def debug(buffer: ByteBuffer) {
    if (buffer.hasRemaining) {
      val builder = StringBuilder.newBuilder
      while (buffer.hasRemaining) {
        builder.append((buffer.get & 0x7F).asInstanceOf[Char])
      }
      println("debug: " + builder.toString())
    }
  }
}
object Test extends HandlerProps {
  def props(client: InetSocketAddress, connection: ActorRef) = Props(classOf[Test], client, connection)
}

