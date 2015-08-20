package com.spooky.bittorrent.protocol.client.pwp.actor

import scala.collection.JavaConversions._
import spooky.util.ByteString
import spooky.actor.Actor
import spooky.io.Tcp.Event
import spooky.actor.ActorRef
import spooky.io.Tcp.Write
import spooky.io.Tcp.CommandFailed
import spooky.io.Tcp.ConnectionClosed
import spooky.io.Tcp
import com.spooky.bittorrent.l.session.client.ClientSession
import spooky.event.Logging
//import spooky.actor.actorRef2Scala
import com.spooky.bittorrent.u.ChokePredictor
import com.spooky.bittorrent.u.Thing
import com.spooky.bittorrent.u.Byte
import scala.Range
import spooky.actor.Props
import com.spooky.bittorrent.Showable
import scala.annotation.tailrec
import spooky.actor.Terminated
import java.util.HashSet
import java.util.TreeSet

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

  override def receive: PartialFunction[Any, Unit] = {
    case CommandFailed(w: Write) => {
      //      val m = w.wantsAck
      //      connection ! ResumeWriting
      //      context become buffering(ack)
      //      log.error("O/S buffesr was full|" + m + "|" + outstanding + "|" + buffering + ":" + w.ack)
      outstanding = (outstanding - 1)
      buffering = true
      println(s"buffering: $buffering| wantsAck:")
      write(w.data, w.ack.asInstanceOf[Ack])
    }
    case a: Ack => {
      //      println(s"NACKK: $a")
      //      log.error(a)
      outstanding = (outstanding - 1)
      chokePredictor.outgoing(a.t)
      //      if ((lastAckSequence + 1) != a.sequence) {
      //        acked.add(a.sequence)
      //        println(s"$lastAckSequence|${a.sequence}")
      //        buffering = true
      //      } else
      lastAckSequence = Math.max(lastAckSequence, a.sequence)
      checkBuffer()
    }
    //    case Tcp.PeerClosed => {
    case e: ConnectionClosed => {
      println(s"outstanding: $outstanding")
    }
    case message: Showable => {
      chokePredictor.incomming(message)
      write(message)
      //      data.apply(a)
    }
    //    case e => {
    //      println("00000000000000000000000000000000000000000" + e)
    //    }
  }
  //  protected def data: PartialFunction[Any, Unit]
  implicit private def write(message: Showable): Unit = {
//    println(s"msg: $message| raw:" + message.toByteBuffer.remaining())
    write(writeCipher.update(message.toByteBuffer), Ack(getSequence, Thing(message)))
  }
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
      def rec(toAck: Long): Unit = {
        val ts = new java.util.TreeSet[Long]
        for (id <- buffer.keySet) {
          if (id <= toAck) {
            ts.add(id)
          }
        }
        for (id <- ts) {
          val v = buffer.remove(id)
          if (v.isDefined) {
            val va = v.get
            write(va._1, va._2)
          }
        }

      }
    if (outstanding == 0 || buffer.size >= 15) {
      buffering = false
      rec(lastAckSequence)
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
