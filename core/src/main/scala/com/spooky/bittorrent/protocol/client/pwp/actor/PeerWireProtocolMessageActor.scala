package com.spooky.bittorrent.protocol.client.pwp.actor

import com.spooky.bittorrent.protocol.client.pwp.api.Handshake
import com.spooky.bittorrent.protocol.client.pwp.api.Bitfield
import com.spooky.bittorrent.protocol.client.pwp.api.Intrested
import com.spooky.bittorrent.protocol.client.pwp.api.KeepAlive
import com.spooky.bittorrent.protocol.client.pwp.api.Have
import com.spooky.bittorrent.protocol.client.pwp.api.Choke
import com.spooky.bittorrent.protocol.client.pwp.api.Unchoke
import com.spooky.bittorrent.protocol.client.pwp.api.NotIntrested
import com.spooky.bittorrent.protocol.client.pwp.api.Piece
import com.spooky.bittorrent.protocol.client.pwp.api.Request
import com.spooky.bittorrent.l.session.client.ClientSession
import com.spooky.bittorrent.l.session.Session
import com.spooky.bittorrent.u.BufferingRetry
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.ImmutableByteBuffer
import akka.event.Logging
import akka.actor.ActorRef
import akka.actor.Props
import com.spooky.cipher.MSEKeyPair

object PeerWireProtocolMessageActor {
  def props(session: Session, connection: ActorRef, handshake: Handshake, keyPair: MSEKeyPair): Props = Props(classOf[PeerWireProtocolMessageActor], session, connection, handshake.peerId, keyPair)
}

class PeerWireProtocolMessageActor(session: Session, connection: ActorRef, peerId: PeerId, keyPair: MSEKeyPair) extends BufferingRetry(connection, new ClientSession(connection), keyPair) { //TODO central session

  private val log = Logging(context.system, this)
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
    //    case p: CommandFailed => log.error(p.cmd.failureMessage)
    case NotIntrested => {
      //      println("outstanding:" + outstanding)
    }
    case a => log.error(s"unahandled message: ${a.getClass}")
  }
}
