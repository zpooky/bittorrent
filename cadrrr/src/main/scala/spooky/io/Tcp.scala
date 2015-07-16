package spooky.io

import scala.collection.JavaConversions._
import java.net.InetSocketAddress
import spooky.actor.ActorRef
import spooky.util.ByteString
import java.nio.channels.ServerSocketChannel
import java.io.IOException
import spooky.actor.Actor
import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import spooky.actor.Terminated
import java.nio.channels.SocketChannel
import spooky.actor.ActorSystem
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap
import spooky.actor.ActorRef
import java.net.InetAddress
import spooky.actor.ActorRef
import java.nio.ByteBuffer
import java.nio.ByteOrder
import spooky.io.TcpThread._
import spooky.actor.Props

/**
 * @author spooky
 */
object Tcp extends Channel {
  def props(implicit actorSystem: ActorSystem): Props = Props(classOf[Tcp], actorSystem)

  abstract class Event {

  }
  object NoAck extends Event
  case class Write(data: ByteString, ack: Event) extends Event
  object Write {
    def apply(data: ByteString): Write = Tcp.Write(data, NoAck)
  }
  case class Bind(handler: ActorRef, localAddress: InetSocketAddress) extends Event
  case class CommandFailed(event: Event) extends Event
  case class Address(address: String, port: Int) {
    def toSocketAddress: SocketAddress = new InetSocketAddress(address, port)
  }
  object Address {
    def apply(address: InetSocketAddress): Address = Tcp.Address(address.getAddress.getHostAddress, address.getPort)
  }
  case class Connected(remoteAddress: Tcp.Address, localAddress: InetSocketAddress) extends Event
  object Connected {
    def apply(remoteAddress: InetSocketAddress, localAddress: InetSocketAddress): Connected = Tcp.Connected(Address(remoteAddress), localAddress)
  }
  case class Register(handler: ActorRef, address: Tcp.Address, keepOpenOnPeerClosed: Boolean = true, useResumeWriting: Boolean = false) extends Event
  class ConnectionClosed extends Event
  case class Received(bs: ByteString) extends Event

}
//TODO should use nonblocking Single Produce Single Consumer queue for received messages
//TODO should use nonblocking Multi Consumer Single consumer queue for send messages
//akka.io.TcpConnection
class Tcp(actorSystem: ActorSystem) extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case bind @ Tcp.Bind(mainTcpActor, localAddress) => {
      try {
        val serverChannel = ServerSocketChannel.open().bind(localAddress)

        val clientChannels = new ConcurrentHashMap[Tcp.Address, SocketChannel]
        val actors = new ConcurrentHashMap[Tcp.Address, Tuple2[MessageActorRef, WriteActorRef]]

        actorSystem.executors.submit(new TcpThread(serverChannel, mainTcpActor, actors, clientChannels, actorSystem))
      } catch {
        case e: Exception => {
          e.printStackTrace
          sender() ! Tcp.CommandFailed(bind)
        }
      }
    }
  }
}

