package spooky.io

import java.nio.channels.SocketChannel
import spooky.actor.Props
import spooky.actor.Actor
import java.nio.ByteBuffer
import spooky.actor.ActorRef

object TcpWriteActor {
  def props(channel: SocketChannel): Props = Props(classOf[TcpWriteActor], channel)
}
private[io] class TcpWriteActor(private val channel: SocketChannel) extends Actor {

  def receive: PartialFunction[Any, Unit] = {
    case Tcp.Write(data, Tcp.NoAck) => {
      write(data.toByteBuffer)
    }
    case Tcp.Write(data, ack) => {
      if (write(data.toByteBuffer)) {
        sender() ! ack
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
