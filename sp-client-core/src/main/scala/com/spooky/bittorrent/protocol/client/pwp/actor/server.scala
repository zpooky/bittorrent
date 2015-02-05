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
import com.spooky.bittorrent.protocol.client.pwp.api.Showable
import com.spooky.bittorrent.protocol.client.pwp.api.Request
import com.spooky.bittorrent.protocol.client.pwp.api.Piece
import com.spooky.bittorrent.ImmutableByteBuffer
import akka.event.Logging
import com.spooky.bittorrent.model.PeerId
import akka.io.Tcp
import java.io.File
import com.spooky.bittorrent.Config
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute
import java.nio.file.StandardOpenOption
import com.spooky.bittorrent.protocol.client.pwp.api.KeepAlive
import com.spooky.bittorrent.protocol.client.pwp.api.Unchoke
import com.spooky.bittorrent.protocol.client.pwp.api.Have
import com.spooky.bittorrent.protocol.client.pwp.api.Choke
import com.spooky.bittorrent.protocol.client.pwp.api.Intrested

abstract class PeerWireProtocolsActors(actorSystem: ActorSystem) {
  //  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  val api: ActorRef = actorSystem.actorOf(Props(classOf[TCPServer], Test))
}
//pstrlen:19|pstr:BitTorrent protocol|reserved:0000000000000000000000000000000000000000000100000000000000000000|info_has:Yj!ﾊXﾰ￾xﾀﾺFlￍﾉￊlwﾏ|peer-id:-lt0D20-Pﾖ￷ﾒﾛﾜￋ￿s

class PeerWireProtocolMessageDeserializerActor(client: InetSocketAddress, connection: ActorRef) extends Actor {
  private val log = new SimpleLog //Logging(context.system, this)
  private var handler: ActorRef = null
  private var first = true

  override def receive = {
    case Received(data) => {
      //      println(":" + data)
      val buffer = data.toByteBuffer.order(ByteOrder.BIG_ENDIAN)
      val cpy = buffer.duplicate
      //      println(client.getHostString + "|" + first)
      val cont: Boolean = if (first) {
        if (buffer.duplicate.get == 19) {
          val handshake = Handshake.parse(buffer)
          first = false
          SessionManager.get(handshake.infoHash) match {
            case Some(sessionManager) => {
              log.debug(s"received: ${handshake}")
              handler = context.actorOf(Props(classOf[PeerWireProtocolMessageActor], sessionManager, connection, handshake.peerId))
              handler ! handshake
              true
            }
            case None => {
              debug(buffer.duplicate())
              println(handshake)
              log.warning("garbage received execpected handshake")
              false
            }
          }
        } else {
          println(cpy)
          val root = new File(new File(Config.getClass.getResource(".").toURI).getAbsoluteFile, "storage")
          println(root)
          Files.createDirectories(root.toPath)
          val file = new File(root, s"${System.currentTimeMillis}.dat").toPath
          println(file)
          Files.write(file, cpy.array(), StandardOpenOption.CREATE_NEW)
          false
        }
      } else true
      if (cont) {
        //not a loop btw
        for {
          received <- PeerWireMessage(buffer)
        } yield {
          log.debug(s"received: ${received}")
          handler ! received
        }
      } else {
        connection ! Tcp.Close
      }
    }
    case (m: ConnectionClosed) => self ! PoisonPill
    case r                     => println("xAxAx:" + r)
  }
  //private def pwpHandler:Props =
  def debug(buffer: ByteBuffer) {
    val builder = StringBuilder.newBuilder
    while (buffer.hasRemaining) {
      builder.append((buffer.get & 0x7F).asInstanceOf[Char])
    }
    println("debug: " + builder.toString())
  }
}
class PeerWireProtocolMessageActor(session: SessionManager, connection: ActorRef, peerId: PeerId) extends Actor {

  private val log = new SimpleLog //Logging(context.system, this)
  private val fileManager = session.fileManager

  override def receive = {
    case Handshake(infoHash, _) => {
      connection ! write(Handshake(infoHash, session.peerId))
      if (fileManager.haveAnyBlocks) {
        connection ! write(Bitfield(fileManager.blocks))
      }
    }
    case Request(index, begin, length) => {
      if (fileManager.have(index)) {
        connection ! write(Piece(index, begin, ImmutableByteBuffer(fileManager.read(index, begin, length))))
      } else {
        log.error(s"remote requested index ${index} but was not present")
      }
    }
    case Bitfield(blocks) => connection ! write(Unchoke)
    case Intrested        => connection ! write(Unchoke)
    case KeepAlive        =>
    case Have(index)      =>
    case Choke            =>
    case Unchoke          =>
    case a                => log.debug(s"unahandled message: ${a.getClass.getSimpleName}")
  }

  def write(message: Showable): Write = {
    log.debug(s"sent ${message}")
    Write(message.toByteString)
  }
}

object Test extends HandlerProps {
  def props(client: InetSocketAddress, connection: ActorRef) = Props(classOf[PeerWireProtocolMessageDeserializerActor], client, connection)
}

class SimpleLog {
  def debug(msg: String) {
    println(msg)
  }
  def warning(msg: String) {
    println(msg)
  }
  def error(msg: String) {
    println(msg)
  }

}
