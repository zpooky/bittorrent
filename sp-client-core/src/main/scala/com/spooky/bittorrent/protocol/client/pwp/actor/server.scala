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
import akka.io.Tcp.CommandFailed
import akka.util.ByteString
import scala.collection.mutable.PriorityQueue
import java.util.concurrent.PriorityBlockingQueue
import akka.io.Tcp.Event
import com.spooky.bittorrent.protocol.client.pwp.api.NotIntrested

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
            case Some(sessionManager) => {
              log.debug(s"received: ${handshake}")
              handler = context.actorOf(Props(classOf[PeerWireProtocolMessageActor], sessionManager, connection, handshake.peerId))
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
        } else {
          log.error(s"handshake failed ${cpy}")
          val root = new File(new File(Config.getClass.getResource(".").toURI).getAbsoluteFile, "storage")
          log.error(root)
          Files.createDirectories(root.toPath)
          val file = new File(root, s"${System.currentTimeMillis}.dat").toPath
          log.error(file)
          Files.write(file, cpy.array(), StandardOpenOption.CREATE_NEW)
          false
        }
      } else true
      if (cont) {
        while (buffer.hasRemaining()) {
          for {
            received <- PeerWireMessage(buffer)
          } yield {
            log.debug(s"received: ${received}")
            handler ! received
          }
        }
        //        log.error(s"tail:${buffer}")
      } else {
        connection ! Tcp.Close
      }
    }
    //    case (m: ConnectionClosed) => self ! PoisonPill
    case r => log.error("xAxAx:" + r)
  }
  //private def pwpHandler:Props =
  def debug(buffer: ByteBuffer) {
    val builder = StringBuilder.newBuilder
    while (buffer.hasRemaining) {
      builder.append((buffer.get & 0x7F).asInstanceOf[Char])
    }
    log.error(s"debug: ${builder}")
  }
}
case class Ack(sequence:Int) extends Event
class PeerWireProtocolMessageActor(session: SessionManager, connection: ActorRef, peerId: PeerId) extends Actor {

  private val log = new SimpleLog //Logging(context.system, this)
  private val fileManager = session.fileManager

  def Ordering = new Ordering[Tuple2[ByteString,Int]] {
    def compare(a : Tuple2[ByteString,Int], b : Tuple2[ByteString,Int]) = a._2.compare(b._2)
  }
  private lazy val buffer = scala.collection.mutable.PriorityQueue[Tuple2[ByteString,Int]]()(Ordering)
  private var outstanding = 0
  private var buffering = false
  private var sequence = 0

  override def receive: PartialFunction[Any, Unit] = {
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
    case CommandFailed(w: Write) => {
      val m = w.wantsAck
      //      connection ! ResumeWriting
      //      context become buffering(ack)
      log.error("O/S buffer was full|" + m + "|" + outstanding+"|"+buffering+":"+w.ack)
      outstanding = (outstanding - 1)
      buffering = true
      write(w.data)
    }
    case a:Ack => {
//      log.error(a)
      outstanding = (outstanding - 1)
      checkBuffer
    }
    case p: CommandFailed => log.error(p.cmd.failureMessage)
    case Tcp.PeerClosed => {
      println("outstanding:"+outstanding)
      context stop self
    }
    case NotIntrested => {
      println("outstanding:"+outstanding)
    }
    case a => log.error(s"unahandled message: ${a.getClass}")
  }

  //    private def buffering: PartialFunction[Any, Unit] = {
  //      case _ =>
  //    }

  private def write(message: Showable): Unit = write(message.toByteString)
  private def write(message: ByteString): Unit = buffering match {
    case true => {
      _buffer((message,getSequence))
    }
    case false => {
      outstanding = (outstanding + 1)
      log.debug(s"sent ${message}")
      connection ! Write(message, Ack(getSequence))
    }
  }
  private def _buffer(message:Tuple2[ByteString,Int]):Unit = {
      buffer += message
      checkBuffer
  }
  private def getSequence:Int ={
    sequence = sequence+1
    sequence
  }
  private def checkBuffer: Unit = {
    if (outstanding == 0 || buffer.size >= 15) {
      buffering = false
      Range(0, buffer.length).foreach { _ =>
        write(buffer.dequeue()._1)
      }
    }
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
