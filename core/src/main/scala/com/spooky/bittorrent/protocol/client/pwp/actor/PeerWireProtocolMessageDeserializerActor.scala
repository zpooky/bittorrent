package com.spooky.bittorrent.protocol.client.pwp.actor

import scala.collection.JavaConversions._
import com.spooky.bittorrent.protocol.client.pwp.tcp.HandlerProps
import java.net.InetSocketAddress
import spooky.actor.ActorRef
import spooky.actor.Props
import spooky.actor.Actor
import spooky.event.Logging
import com.spooky.inbound.InStep
import spooky.io.Tcp.Received
import com.spooky.inbound.step.InboundStep
import com.spooky.inbound.Reply
import spooky.util.ByteString
import com.spooky.inbound.step.DoneStep
import com.spooky.bittorrent.l.session.Sessions
import spooky.io.Tcp.Write
import com.spooky.cipher.ReadPlain
import com.spooky.cipher.WritePlain
import com.spooky.cipher.MSEKeyPair
import com.spooky.bittorrent.protocol.client.pwp.api.Handshake
import spooky.actor.Terminated
import spooky.io.Tcp

//pstrlen:19|pstr:BitTorrent protocol|reserved:0000000000000000000000000000000000000000000100000000000000000000|info_has:Yj!ﾊXﾰ￾xﾀﾺFlￍﾉￊlwﾏ|peer-id:-lt0D20-Pﾖ￷ﾒﾛﾜￋ￿s

object PeerWireProtocolMessageDeserializerActor {

  def props(sessions: Sessions): HandlerProps = new HandlerProps {
    def props(client: Tcp.Address, connection: ActorRef): Props = PeerWireProtocolMessageDeserializerActor.propsx(sessions)(client, connection)
  }

  private def propsx(sessions: Sessions)(client: Tcp.Address, connection: ActorRef) = Props(classOf[PeerWireProtocolMessageDeserializerActor], client, connection, sessions)
}

class PeerWireProtocolMessageDeserializerActor(client: Tcp.Address, connection: ActorRef, sessions: Sessions) extends Actor {
  private val log = Logging(context.system, this)
  private var handler: ActorRef = null

  // sign death pact: this actor terminates when connection breaks
  context watch connection

  private var step: InStep = null
  //  var become = false
  override def receive: PartialFunction[Any, Unit] = {
    case request @ Received(data) => {
      //      println(":" + data)
      //      val buffer = data.toByteBuffer.order(ByteOrder.BIG_ENDIAN)
      //      val cpy = buffer.duplicate
      //      println(client.getHostString + "|" + first)
      if (step == null && data.head == 19 && data.length == 68) {
        val msg = new MessageParserControl(connection, this)(MSEKeyPair(WritePlain, ReadPlain), sessions)
        msg.receive(request)
      } else {
        step = if (step == null) {
          new InboundStep(sessions.infoHashes)
        } else step
        step = step.step(data).step(exchange)
        step match {
          case d: DoneStep => {
            step = null
            val msg = new MessageParserControl(connection, this)(d.keyPair, sessions)
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
  }

  private def exchange: Reply = new Reply {
    def reply(r: ByteString): Unit = {
      connection ! Write(r)
    }
  }
}

