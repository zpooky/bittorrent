package com.spooky.bittorrent.protocol.client.pwp.actor

import scala.collection.JavaConversions._
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
import akka.io.Tcp.Write
import com.spooky.bittorrent.protocol.client.pwp.api.Bitfield
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
import akka.io.Tcp.CommandFailed
import akka.util.ByteString
import scala.collection.mutable.PriorityQueue
import java.util.concurrent.PriorityBlockingQueue
import akka.io.Tcp.Event
import com.spooky.bittorrent.protocol.client.pwp.api.NotIntrested
import com.spooky.BParty
import com.spooky.bittorrent.mse.PublicKey
import com.spooky.bittorrent.l.session.SessionManager
import com.spooky.bittorrent.l.session.Session
import com.spooky.bittorrent.u.BufferingRetry
import com.spooky.bittorrent.l.session.Session
import com.spooky.bittorrent.l.session.client.ClientSession

abstract class PeerWireProtocolsActors(actorSystem: ActorSystem) {
  //  private val test: ActorRef = actorSystem.actorOf(Props[Test]())
  val api: ActorRef = actorSystem.actorOf(Props(classOf[TCPServer], Test))
}
//pstrlen:19|pstr:BitTorrent protocol|reserved:0000000000000000000000000000000000000000000100000000000000000000|info_has:Yj!ﾊXﾰ￾xﾀﾺFlￍﾉￊlwﾏ|peer-id:-lt0D20-Pﾖ￷ﾒﾛﾜￋ￿s

class PeerWireProtocolMessageDeserializerActor(client: InetSocketAddress, connection: ActorRef) extends Actor {
  private val log = new SimpleLog //Logging(context.system, this)
  private var handler: ActorRef = null
  private var first = true

  // sign death pact: this actor terminates when connection breaks
  context watch connection

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
            case Some(session) => {
              log.debug(s"received: ${handshake}")
              handler = context.actorOf(Props(classOf[PeerWireProtocolMessageActor], session, connection, handshake.peerId))
              handler ! handshake
              true
            }
            case None => {
              debug(buffer.duplicate())
              log.error(handshake)
              log.warning("garbage received execpected handshake")
              false
            }
          }
        } else { //Encrypted
          val sessionManager = SessionManager.test
          //            val b = new BParty(sessionManager.infoHash)
          //            b.receivePublic(PublicKey.raw(buffer))
          //
          //            a.receivePublic(b.sendPublic)
          //
          //                println("----")
          //                b.receive(a.send)
          //                a.receive(b.send)
          //
          //                b._3(a._3)
          //
          //                println("----")
          //                b.receive(a.send)
          //                a.receive(b.send)
          //
          //                a._4(b._4)
          //
          //                println("----")
          //                b.receive(a.send)
          //                a.receive(b.send)

          //          log.error(s"handshake failed ${cpy}")
          //          val root = new File(new File(Config.getClass.getResource(".").toURI).getAbsoluteFile, "storage")
          //          log.error(root)
          //          Files.createDirectories(root.toPath)
          //          val file = new File(root, s"${System.currentTimeMillis}.dat").toPath
          //          log.error(file)
          //          Files.write(file, cpy.array(), StandardOpenOption.CREATE_NEW)
          false
        }
      } else true
      if (cont) {
        for {
          received <- PeerWireMessage.parse(buffer)
        } yield {
          log.debug(s"received: ${received}")
          handler ! received
        }
        //        log.error(s"tail:${buffer}")
      } else {
        connection ! Tcp.Close
      }
    }
    //    case (m: ConnectionClosed) => self ! PoisonPill
//    case r => log.error("xAxAx:" + r)
  }
  //private def pwpHandler:Props =
  def debug(buffer: ByteBuffer) {
    val builder = StringBuilder.newBuilder
    while (buffer.hasRemaining) {
      builder.append((buffer.get & 0x7F).asInstanceOf[Char])
    }
    log.error(s"debug: ${buffer}|${builder}")
  }
}
case class Ack(sequence: Int) extends Event
class PeerWireProtocolMessageActor(session: Session, connection: ActorRef, peerId: PeerId) extends BufferingRetry(connection, new ClientSession(connection)) {//TODO central session

  private val log = new SimpleLog //Logging(context.system, this)
  private val fileManager = session.fileManager

  override def data: PartialFunction[Any, Unit] = {
    case Handshake(infoHash, _) => {
      write(Handshake(infoHash, session.peerId))
      if (fileManager.haveAnyBlocks) {
        write(Bitfield(fileManager.blocks))
      }
    }
    case Request(index, begin, length) => {
      if (fileManager.have(index)) {
        write(Piece(index, begin, ImmutableByteBuffer(fileManager.read(index, begin, length))))
      } else {
        log.error(s"remote requested index ${index} but was not present")
      }
    }
    case Bitfield(blocks) => write(Unchoke)
    case Intrested        => write(Unchoke)
    case KeepAlive        =>
    case Have(index)      =>
    case Choke            =>
    case Unchoke          =>
    case p: CommandFailed => log.error(p.cmd.failureMessage)
    case NotIntrested => {
      //      println("outstanding:" + outstanding)
    }
    case a => log.error(s"unahandled message: ${a.getClass}")
  }
}

object Test extends HandlerProps {
  def props(client: InetSocketAddress, connection: ActorRef) = Props(classOf[PeerWireProtocolMessageDeserializerActor], client, connection)
}

class SimpleLog {
  def debug(msg: Any) {
    //    println(msg)
  }
  def warning(msg: Any) {
    println(msg)
  }
  def error(msg: Any) {
    println(msg)
  }

}
