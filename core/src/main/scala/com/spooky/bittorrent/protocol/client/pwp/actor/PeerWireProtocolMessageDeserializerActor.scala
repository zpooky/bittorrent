package com.spooky.bittorrent.protocol.client.pwp.actor

import scala.collection.JavaConversions._
import com.spooky.bittorrent.protocol.client.pwp.tcp.HandlerProps
import java.net.InetSocketAddress
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Actor
import akka.event.Logging
import com.spooky.inbound.InStep
import akka.io.Tcp.Received
import com.spooky.inbound.step.InboundStep
import com.spooky.inbound.Reply
import akka.util.ByteString
import com.spooky.inbound.step.DoneStep
import com.spooky.bittorrent.l.session.SessionManager
import akka.io.Tcp.Write
import com.spooky.cipher.ReadPlain
import com.spooky.cipher.WritePlain
import com.spooky.cipher.MSEKeyPair
import com.spooky.bittorrent.protocol.client.pwp.api.Handshake
import akka.actor.Terminated

//pstrlen:19|pstr:BitTorrent protocol|reserved:0000000000000000000000000000000000000000000100000000000000000000|info_has:Yj!ﾊXﾰ￾xﾀﾺFlￍﾉￊlwﾏ|peer-id:-lt0D20-Pﾖ￷ﾒﾛﾜￋ￿s

object PeerWireProtocolMessageDeserializerActor extends HandlerProps {
  def props(client: InetSocketAddress, connection: ActorRef) = Props(classOf[PeerWireProtocolMessageDeserializerActor], client, connection)
}

class PeerWireProtocolMessageDeserializerActor(client: InetSocketAddress, connection: ActorRef) extends Actor {
  private val log = Logging(context.system, this)
  private var handler: ActorRef = null

  // sign death pact: this actor terminates when connection breaks
  context watch connection

  private var step: InStep = null
  //  var become = false
  override def receive = {
    case request @ Received(data) => {
      //      println(":" + data)
      //      val buffer = data.toByteBuffer.order(ByteOrder.BIG_ENDIAN)
      //      val cpy = buffer.duplicate
      //      println(client.getHostString + "|" + first)
      if (step == null && data.head == 19 && data.length == 68) {
        val msg = new MessageParserControl(connection, this)(MSEKeyPair(WritePlain, ReadPlain))
        msg.receive(request)
      } else {
        step = if (step == null) {
          new InboundStep(SessionManager.infoHashes)
        } else step
        step = step.step(data).step(exchange)
        step match {
          case d: DoneStep => {
            step = null
            val msg = new MessageParserControl(connection, this)(d.keyPair)
            if (d.data.isDefined) {
              msg.receive(Handshake.parse(d.data.get))
            } else {
              context.become(msg.receive)
            }
          }
          case _ =>
        }
      }
    }
    case e: Terminated => {

    }
  }

  private def exchange: Reply = new Reply {
    def reply(r: ByteString): Unit = {
      connection ! Write(r)
    }
  }
}

