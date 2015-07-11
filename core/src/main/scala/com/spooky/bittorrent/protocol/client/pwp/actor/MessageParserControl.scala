package com.spooky.bittorrent.protocol.client.pwp.actor

import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging
import akka.actor.Actor
import akka.io.Tcp.Received
import com.spooky.bittorrent.protocol.client.pwp.api.Handshake
import com.spooky.bittorrent.l.session.SessionManager
import com.spooky.bittorrent.protocol.client.pwp.api.PeerWireMessage
import akka.actor.ActorContext
import com.spooky.cipher.MSEKeyPair
import com.spooky.cipher.WriteCipher
import com.spooky.cipher.ReadCipher
import akka.actor.Terminated

class MessageParserControl(connection: ActorRef, actor: Actor)(keyPair: MSEKeyPair)(implicit context: ActorContext) {
  private val readCipher = keyPair.readCipher
  private val log = Logging(context.system, actor)
  private var handler: ActorRef = null //TODO construct

  context watch connection

  def receive: Actor.Receive = {
    case Received(data) => {
      val readCipher = keyPair.readCipher
      val handshake = Handshake.parse(readCipher.update(data))
      handle(handshake)
    }
    case handshake: Handshake => handle(handshake)
    case e: Terminated => {

    }
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

  private def trafic: Actor.Receive = {
    case Received(data) => {
      for {
        received <- PeerWireMessage.parse(readCipher.update(data))
      } yield {
        log.debug(s"received: ${received}")
        handler ! received
      }
    }
  }
}
