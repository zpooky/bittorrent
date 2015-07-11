package com.spooky.bittorrent.protocol.client.pwp.actor

import akka.util.ByteString
import akka.actor.Actor
import akka.io.Tcp.Event
import akka.actor.ActorRef
import akka.io.Tcp.Write
import akka.io.Tcp.CommandFailed
import akka.io.Tcp
import com.spooky.bittorrent.l.session.client.ClientSession
import akka.event.Logging
import akka.actor.actorRef2Scala
import com.spooky.bittorrent.u.ChokePredictor
import com.spooky.bittorrent.u.Thing
import com.spooky.bittorrent.u.Byte
import scala.Range
import akka.actor.Props
import com.spooky.bittorrent.Showable
import scala.annotation.tailrec
import akka.actor.Terminated
import java.util.HashSet

case class Ack(sequence: Long, t: Thing) extends Event

object BufferingRetryActor {
  def props(connection: ActorRef, session: ClientSession): Props = Props(classOf[BufferingRetryActor], connection, session)
}

class BufferingRetryActor(connection: ActorRef, session: ClientSession) extends Actor {
  private val writeCipher = session.keyPair.writeCipher
  private val log = Logging(context.system, this)
  private val chokePredictor = new ChokePredictor(Byte(65536), /*Size(2, MegaByte), */ session) //TODO find out a way to get buffer size

  context watch connection

  private def ordering = new Ordering[Tuple2[ByteString, Ack]] {
    def compare(a: Tuple2[ByteString, Ack], b: Tuple2[ByteString, Ack]) = a._2.sequence.compare(b._2.sequence)
  }
  //  private lazy val buffer = scala.collection.mutable.PriorityQueue[Tuple2[ByteString, Ack]]()(ordering)
  private lazy val buffer = scala.collection.mutable.Map[Long, Tuple2[ByteString, Ack]]()
  private var outstanding: Long = 0
  private var buffering = false
  private var sequence: Long = 0
  private var lastAckSequence = sequence - 1
  private val acked = new HashSet[Long]

  override def receive: Actor.Receive = {
    case CommandFailed(w: Write) => {
      val m = w.wantsAck
      //      connection ! ResumeWriting
      //      context become buffering(ack)
      //      log.error("O/S buffesr was full|" + m + "|" + outstanding + "|" + buffering + ":" + w.ack)
      outstanding = (outstanding - 1)
      buffering = true
      println(s"buffering: $buffering| wantsAck: $m")
      write(w.data, w.ack.asInstanceOf[Ack])
    }
    case a: Ack => {
      //      println(s"NACKK: $a")
      //      log.error(a)
      outstanding = (outstanding - 1)
      chokePredictor.outgoing(a.t)
      if ((lastAckSequence + 1) != a.sequence) {
        acked.add(a.sequence)
        println(s"$lastAckSequence|${a.sequence}")
        buffering = true
      } else lastAckSequence = Math.max(lastAckSequence, a.sequence)
      checkBuffer()
    }
    //    case Tcp.PeerClosed => {
    case e: Tcp.ConnectionClosed => {
      println(s"outstanding: $outstanding")
      context stop self
    }
    case e: Terminated => {
      println(s"outstanding: $outstanding|buffer ${buffer.keySet.size}| acked: ${acked.size()}")
    }
    case message: Showable => {
      //      println(s"msg: $message")
      chokePredictor.incomming(message)
      write(message)
      //      data.apply(a)
    }
    case e => {
      println("00000000000000000000000000000000000000000" + e)
    }
  }
  //  protected def data: PartialFunction[Any, Unit]
  implicit private def write(message: Showable): Unit = write(writeCipher.update(message.toByteBuffer), Ack(getSequence, Thing(message)))
  private def write(message: ByteString, ack: Ack): Unit = if (buffering && outstanding > 0) {
    _buffer(message, ack)
  } else {
    buffering = false
    //      println(s"sent ${message}")
    outstanding = (outstanding + 1)
    //      log.debug(s"sent ${message}")
    connection ! Write(message, ack)
  }

  private def _buffer(message: ByteString, ack: Ack): Unit = {
    buffer.put(ack.sequence, (message, ack))
    checkBuffer
  }
  private def getSequence: Long = {
    val result = sequence
    sequence = sequence + 1
    result
  }
  private def checkBuffer(): Unit = {
      @tailrec
      def rec(toAck: Long): Long = buffer.remove(toAck) match {
        case Some(t) => {
          write(t._1, t._2)
          rec(toAck + 1)
        }
        case None => {
          if (acked.remove(toAck)) {
            rec(toAck + 1)
          } else toAck
        }
      }
    if (outstanding == 0 || buffer.size >= 15) {
      buffering = false
      lastAckSequence = rec(lastAckSequence)
      if (!buffer.isEmpty) {
        buffering = true
        //        println(s"buffering: $buffering")
      }
      //      val request = buffer.get(lastAckSequence)
      //      for (_ <- 0 until buffer.length) {
      //        val v = buffer.dequeue()
      //        write(v._1, v._2)
      //      }
    }
  }
}
