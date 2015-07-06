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
import akka.io.Tcp.Write

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
        val msg = new MessageParserControll(connection, this)
        msg.receive(request)
      } else {
        step = if (step == null) {
          new InboundStep(SessionManager.test)
        } else step
        val reply = exchange
        step = step.step(data, reply).step(reply)
        step match {
          case d: DoneStep => {
            context.become(encrypted)
          }
          case _ => println("!!!!BASE")
        }
      }
    }
  }

  private def exchange: Reply = new Reply {
    def reply(r: ByteString): Unit = {
      println("!!!SENT!!!" + r.length)
      connection ! Write(r)
    }
  }

  private def encrypted: Actor.Receive = {
    case Received(data) => {
      println(data)
    }
  }
}

