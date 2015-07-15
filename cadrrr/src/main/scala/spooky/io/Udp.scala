package spooky.io

import spooky.util.ByteString
import spooky.actor.ActorRef
import java.net.InetSocketAddress
import spooky.actor.Actor
import spooky.actor.ActorSystem
import spooky.actor.Props

/**
 * @author spooky
 */
object Udp extends Channel {
  def props(implicit actorSystem: ActorSystem): Props = Props(classOf[Udp])

  abstract class Event {

  }
  object NoAck extends Event
  //  case class Write(data: ByteString, ack: Event) extends Event
  //  object Write {
  //    def apply(data: ByteString): Write = Write(data, NoAck)
  //  }
  case class Bind(actorRef: ActorRef, localAddress: InetSocketAddress) extends Event
  case class CommandFailed(event: Event) extends Event
  object Unbound extends Event
  object Unbind extends Event
  object Bound extends Event
  object SimpleSenderReady extends Event
  case class Received(data: ByteString, remote: InetSocketAddress) extends Event
  case class Send(data: ByteString, destinationAddress: InetSocketAddress) extends Event
}
class Udp extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    ???
  }
}
