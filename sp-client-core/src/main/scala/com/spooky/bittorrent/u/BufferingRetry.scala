package com.spooky.bittorrent.u

import akka.util.ByteString
import akka.actor.Actor
import akka.io.Tcp.Event
import akka.actor.ActorRef
import akka.io.Tcp.Write
import akka.io.Tcp.CommandFailed
import akka.io.Tcp
import com.spooky.bittorrent.protocol.client.pwp.actor.SimpleLog
import com.spooky.bittorrent.l.session.client.ClientSession
import com.spooky.bittorrent.Showable

case class Ack(sequence: Int, t: Thing) extends Event
abstract class BufferingRetry(connection: ActorRef, session: ClientSession) extends Actor {

  private val log = new SimpleLog //Logging(context.system, this)
  private val chokePredictor = new ChokePredictor(KiloByte(63), /*Size(2, MegaByte), */ session) //TODO find out a way to get buffer size

  def Ordering = new Ordering[Tuple2[ByteString, Ack]] {
    def compare(a: Tuple2[ByteString, Ack], b: Tuple2[ByteString, Ack]) = a._2.sequence.compare(b._2.sequence)
  }
  private lazy val buffer = scala.collection.mutable.PriorityQueue[Tuple2[ByteString, Ack]]()(Ordering)
  private var outstanding = 0
  private var buffering = false
  private var sequence = 0

  override final def receive: PartialFunction[Any, Unit] = {
    case CommandFailed(w: Write) => {
      val m = w.wantsAck
      //      connection ! ResumeWriting
      //      context become buffering(ack)
      //      log.error("O/S buffesr was full|" + m + "|" + outstanding + "|" + buffering + ":" + w.ack)
      outstanding = (outstanding - 1)
      buffering = true
      write(w.data, w.ack.asInstanceOf[Ack].t)
    }
    case a: Ack => {
      //      log.error(a)
      outstanding = (outstanding - 1)
      chokePredictor.outgoing(a.t)
      checkBuffer
    }
    //    case Tcp.PeerClosed => {
    case e: Tcp.ConnectionClosed => {
      println("outstanding:" + outstanding)
      context stop self
    }
    case a: Any => {
      chokePredictor.incomming(a)
      data.apply(a)
    }
  }
  def data: PartialFunction[Any, Unit]
  protected def write(message: Showable): Unit = write(message.toByteString, Thing(message))
  private def write(message: ByteString, t: Thing): Unit = buffering match {
    case true => {
      _buffer((message, Ack(getSequence, t)))
    }
    case false => {
      outstanding = (outstanding + 1)
      //      log.debug(s"sent ${message}")
      connection ! Write(message, Ack(getSequence, t))
    }
  }
  private def _buffer(message: Tuple2[ByteString, Ack]): Unit = {
    buffer += message
    checkBuffer
  }
  private def getSequence: Int = {
    sequence = sequence + 1
    sequence
  }
  private def checkBuffer: Unit = {
    if (outstanding == 0 || buffer.size >= 15) {
      buffering = false
      Range(0, buffer.length).foreach { _ =>
        val v = buffer.dequeue()
        write(v._1, v._2.t)
      }
    }
  }
}
