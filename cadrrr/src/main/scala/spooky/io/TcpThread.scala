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
import java.io.IOException

object TcpThread {
  type MessageActorRef = ActorRef
  type WriteActorRef = ActorRef
}

private[io] class TcpThread(serverChannel: ServerSocketChannel, mainTcpActor: ActorRef)(actorSystem: ActorSystem) extends Runnable {

  private val clientChannels = new ConcurrentHashMap[Tcp.Address, SocketChannel]
  private val actors = new ConcurrentHashMap[Tcp.Address, Tuple2[MessageActorRef, WriteActorRef]]

  def run(): Unit = {
    serverChannel.configureBlocking(false)
    val selector = Selector.open
    val serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT) // | SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE
    val buffer = ByteBuffer.allocate(1024 * 8).order(ByteOrder.BIG_ENDIAN)
    while (true) {
      buffer.clear()
      //blocking
      //      println("keys: " + selector.keys().size())
      val available = selector.select()
      val keySet = selector.keys()
      val keyIterator = keySet.iterator()
      while (keyIterator.hasNext()) {
        val current = keyIterator.next()
        try {
          if (current.channel().isInstanceOf[ServerSocketChannel]) {
            //            if (current.isAcceptable) {
            accept(current.channel().asInstanceOf[ServerSocketChannel], selector)
            //            }
          } else {
            val clientChannel = current.channel().asInstanceOf[SocketChannel]
            val clientSocket = clientChannel.socket
            try {
              //              if (!clientChannel.isOpen() || clientSocket.isClosed() || !clientSocket.isConnected()) {
              //                terminate(clientChannel)
              //                keyIterator.remove()
              //              } else

              //              if (current.isReadable) {
              receive(clientChannel, buffer)
              //              }
            } catch {
              case e: IOException => {
                e.printStackTrace()
                terminate(clientChannel)
                //                keyIterator.remove()
              }
              case e: Throwable => e.printStackTrace()
            }
          }
        } catch {
          case e: IOException => {
            e.printStackTrace()
            //            keyIterator.remove()
          }
          case e: Throwable => e.printStackTrace()
        }
      }
      //      keys.clear()
    }

  }

  private def accept(sc: ServerSocketChannel, selector: Selector): Unit = {
    val client = sc.accept()
    if (client != null) {
      client.configureBlocking(false)
      client.register(selector, SelectionKey.OP_READ, SelectionKey.OP_WRITE)

      val clientAddress = Tcp.Address(client.getRemoteAddress.asInstanceOf[InetSocketAddress])
      println("----------" + clientAddress)
      clientChannels.put(clientAddress, client)

      val writeActor = actorSystem.actorOf(TcpWriteActor.props(client, actors))
      mainTcpActor.!(Tcp.Connected(clientAddress, client.getLocalAddress.asInstanceOf[InetSocketAddress]))(writeActor)
      println("---sent")
    }
  }

  private def receive(clientChannel: SocketChannel, buffer: ByteBuffer): Unit = {
    val handlers = actorsFor(clientChannel)
    if (handlers != null) {
      val (messageActor, writeActor) = handlers
      if (clientChannel.read(buffer) != -1) {
        if (buffer.position() != 0) {
          messageActor.!(Tcp.Received(ByteString.copy(buffer.flip())))(writeActor)
        }
      } else {
        terminate(clientChannel)
      }
    }
  }

  private def terminate(clientChannel: SocketChannel): Unit = {
    val clientAddress = Tcp.Address(clientChannel.getRemoteAddress.asInstanceOf[InetSocketAddress])

    val handlers = actors.remove(clientAddress) //make better
    clientChannels.remove(clientAddress)

    if (handlers != null) {
      val (messageActor, writeActor) = handlers
      messageActor ! new Tcp.ConnectionClosed
      messageActor ! Terminated(null)
      writeActor ! Terminated(null)
    }
    clientChannel.close()
  }

  private def actorsFor(clientChannel: SocketChannel): Tuple2[MessageActorRef, WriteActorRef] = actors.get(Tcp.Address(clientChannel.getRemoteAddress.asInstanceOf[InetSocketAddress]))
}
