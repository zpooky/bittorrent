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

object MessageParserActor {
  def props(connection: ActorRef) = Props(classOf[MessageParserActor], connection)
}
class MessageParserActor(connection: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private var handler: ActorRef = null //TODO construct

  override def receive = {
    case Received(data) => {
      val handshake = Handshake.parse(data.toByteBuffer) //TODO
      SessionManager.get(handshake.infoHash) match {
        case Some(session) => {
          log.debug(s"received: ${handshake}")
          handler = context.actorOf(Props(classOf[PeerWireProtocolMessageActor], session, connection, handshake.peerId))
          handler ! handshake
          context.become(trafic)
        }
        case None => {
          //              debug(buffer.duplicate())
          //          log.error(handshake)
          log.warning("garbage received execpected handshake")
        }
      }
    }
  }

  private def trafic: Receive = {
    case Received(data) => {
      for {
        received <- PeerWireMessage.parse(data.toByteBuffer) //TODO support bytestring
      } yield {
        log.debug(s"received: ${received}")
        handler ! received
      }
    }
  }
}

class MessageParserControll(connection: ActorRef, actor: Actor)(implicit context: ActorContext) {
  private val log = Logging(context.system, actor)
  private var handler: ActorRef = null //TODO construct

  def receive: Actor.Receive = {
    case Received(data) => {
      val handshake = Handshake.parse(data.toByteBuffer) //TODO
      SessionManager.get(handshake.infoHash) match {
        case Some(session) => {
          log.debug(s"received: ${handshake}")
          context.become(trafic)
          handler = context.actorOf(PeerWireProtocolMessageActor.props(session, connection, handshake))
          handler ! handshake
        }
        case None => {
          //              debug(buffer.duplicate())
          //          log.error(handshake)
          log.warning("garbage received execpected handshake")
        }
      }
    }
  }

  private def trafic: Actor.Receive = {
    case Received(data) => {
      for {
        received <- PeerWireMessage.parse(data.toByteBuffer) //TODO support bytestring
      } yield {
        log.debug(s"received: ${received}")
        handler ! received
      }
    }
  }
}
