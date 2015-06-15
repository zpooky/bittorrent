package com.spooky.bittorrent.protocol.client.pwp.actor

import akka.actor.Actor
import akka.actor.ActorRef
import java.nio.ByteBuffer

case class MSEHandshake(buffer: ByteBuffer)
class MessageStreamEncryptionActor(connection: ActorRef, peerWireProtocolMessageActor: ActorRef) extends Actor {

  override def receive: PartialFunction[Any, Unit] = {
    case MSEHandshake(buffer) =>
    case _                    =>
  }
  private def rawKey(buffer: ByteBuffer): Unit = {

  }
}
