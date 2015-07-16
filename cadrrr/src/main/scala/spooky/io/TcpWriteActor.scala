package spooky.io

import java.nio.channels.SocketChannel
import spooky.actor.Props
import spooky.actor.Actor
import java.nio.ByteBuffer
import spooky.actor.ActorRef
import spooky.io.TcpThread._
import java.util.concurrent.ConcurrentHashMap

object TcpWriteActor {
  def props(channel: SocketChannel, actors: ConcurrentHashMap[Tcp.Address, Tuple2[MessageActorRef, WriteActorRef]]): Props = Props(classOf[TcpWriteActor], channel, actors)
}
private[io] class TcpWriteActor(private val channel: SocketChannel, actors: ConcurrentHashMap[Tcp.Address, Tuple2[MessageActorRef, WriteActorRef]]) extends Actor {

  def receive: PartialFunction[Any, Unit] = {
    case Tcp.Register(messageActor, address, _, _) => {
      actors.put(address, (messageActor, self))
      context.become(traffic())
    }

  }
  private def traffic(): PartialFunction[Any, Unit] = {
    case Tcp.Write(data, Tcp.NoAck) => {
      write(data.toByteBuffer)
    }
    case Tcp.Write(data, ack) => {
      if (write(data.toByteBuffer)) {
        sender ! ack
      }
    }
  }

  private def write(bb: ByteBuffer): Boolean = {
    if (!channel.isConnected) {
      Thread.currentThread.interrupt()
      false
    } else {
      while (bb.hasRemaining()) {
        channel.write(bb)
      }
      true
    }
  }
}
