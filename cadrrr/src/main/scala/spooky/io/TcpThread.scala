package spooky.io

import scala.collection.JavaConversions._
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.ConcurrentHashMap
import spooky.actor.ActorRef
import java.nio.channels.SelectionKey
import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import spooky.util.ByteString
import java.net.InetSocketAddress
import spooky.actor.Terminated
import spooky.io.TcpThread._
import spooky.actor.ActorContext
import spooky.actor.ActorSystem

object TcpThread {
  type MessageActorRef = ActorRef
  type WriteActorRef = ActorRef
}

private[io] class TcpThread(serverChannel: ServerSocketChannel, mainTcpActor: ActorRef, actors: ConcurrentHashMap[Tcp.Address, Tuple2[MessageActorRef, WriteActorRef]], clientChannels: ConcurrentHashMap[Tcp.Address, SocketChannel], actorSystem: ActorSystem) extends Runnable {
  def run(): Unit = {
    serverChannel.configureBlocking(false)
    val selector = Selector.open
    val serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT) // | SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE
    val buffer = ByteBuffer.allocate(1024 * 8).order(ByteOrder.BIG_ENDIAN)
    while (true) {
      buffer.clear()
      //blocking
      val available = selector.select()
      val keys = selector.keys().iterator()
      while (keys.hasNext()) {
        val current = keys.next()
        try {
          if (current.channel().isInstanceOf[ServerSocketChannel]) { //current.isAcceptable
            accept(selector)
          } else {
            val clientChannel = current.channel().asInstanceOf[SocketChannel]
            val clientSocket = clientChannel.socket
            if (!clientChannel.isOpen()) {
              terminate(clientChannel)
              keys.remove()
            } else if (current.isReadable) {
              receive(clientChannel, buffer)
            }
          }
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
      //      keys.clear()
    }

  }

  private def accept(selector: Selector): Unit = {
    val client = serverChannel.accept()
    if (client != null) {
      client.configureBlocking(false)
      client.register(selector, SelectionKey.OP_READ, SelectionKey.OP_WRITE)

      val clientAddress = Tcp.Address(client.getRemoteAddress.asInstanceOf[InetSocketAddress])
      clientChannels.put(clientAddress, client)

      val writeActor = actorSystem.actorOf(TcpWriteActor.props(client, actors))
      mainTcpActor.!(Tcp.Connected(clientAddress, client.getLocalAddress.asInstanceOf[InetSocketAddress]))(writeActor)
    }
  }

  private def terminate(clientChannel: SocketChannel): Unit = {
    val clientAddress = Tcp.Address(clientChannel.getRemoteAddress.asInstanceOf[InetSocketAddress])

    val handlers = actors.remove(clientAddress) //make better
    clientChannels.remove(clientAddress)

    if (handlers != null) {
      val (messageActor, writeActor) = handlers
      messageActor ! Terminated(null)
      writeActor ! Terminated(null)
    }
    clientChannel.close()
  }

  private def receive(clientChannel: SocketChannel, buffer: ByteBuffer): Unit = {
    val handlers = actorsFor(clientChannel)
    if (handlers != null) {
      val (messageActor, writeActor) = handlers
      clientChannel.read(buffer)
      if (buffer.position() != 0) {
        messageActor.!(Tcp.Received(ByteString.copy(buffer.flip())))(writeActor)
      }
    }
  }

  private def actorsFor(clientChannel: SocketChannel): Tuple2[MessageActorRef, WriteActorRef] = actors.get(Tcp.Address(clientChannel.getRemoteAddress.asInstanceOf[InetSocketAddress]))
}
