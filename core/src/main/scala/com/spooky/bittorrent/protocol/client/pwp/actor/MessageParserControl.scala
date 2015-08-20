package com.spooky.bittorrent.protocol.client.pwp.actor

import scala.collection.JavaConversions._
import spooky.actor.ActorRef
import spooky.actor.Props
import spooky.event.Logging
import spooky.actor.Actor
import spooky.io.Tcp.Received
import com.spooky.bittorrent.protocol.client.pwp.api.Handshake
import com.spooky.bittorrent.l.session.SessionManager
import com.spooky.bittorrent.protocol.client.pwp.api.PeerWireMessage
import spooky.actor.ActorContext
import com.spooky.cipher.MSEKeyPair
import com.spooky.cipher.WriteCipher
import com.spooky.cipher.ReadCipher
import spooky.actor.Terminated
import java.nio.ByteBuffer
import spooky.util.ByteString

class MessageParserControl(connection: ActorRef, actor: Actor)(keyPair: MSEKeyPair)(implicit context: ActorContext) {
  private val readCipher = keyPair.readCipher
  private val log = Logging(context.system, actor)
  private var handler: ActorRef = null //TODO construct

  private var rest: ByteBuffer = null

  private implicit val self = actor.self

  context watch connection

  def receive: PartialFunction[Any, Unit] = {
    case Received(data) => {
      val readCipher = keyPair.readCipher
      val handshake = Handshake.parse(readCipher.update(data))
      handle(handshake)
    }
    case handshake: Handshake => handle(handshake)
  }

  private def handle(handshake: Handshake): Unit = SessionManager.get(handshake.infoHash) match {
    case Some(session) => {
      log.debug(s"received: ${handshake}")
      handler = context.actorOf(PeerWireProtocolMessageActor.props(session, connection, handshake, keyPair))
      handler ! handshake
      context.become(trafic)
    }
    case None => {
      //              debug(buffer.duplicate())
      //          log.error(handshake)
      println("garbage received execpected handshake")
    }
  }

  private def trafic: PartialFunction[Any, Unit] = {
    case Received(encoded) => {
        def decode(encoded: ByteString): ByteBuffer = {
          val decoded = readCipher.updateBB(encoded)
          if (rest != null) {
            val result = ByteBuffer.allocate(rest.remaining() + decoded.remaining())
            result.put(rest)
            result.put(decoded)
            rest = null
            result
          } else decoded
        }
//      val data = decode(encoded)
      for {
//        received <- PeerWireMessage.parse(data)
                received <- PeerWireMessage.parse(readCipher.update(encoded))
      } yield {
        log.debug(s"received: ${received}")
        handler ! received
      }
//      if (data.hasRemaining()) {
//        rest = ByteBuffer.allocate(data.remaining())
//        rest.put(data)
//      }
    }
  }
}
