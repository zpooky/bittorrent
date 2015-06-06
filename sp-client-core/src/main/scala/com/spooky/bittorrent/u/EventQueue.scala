package com.spooky.bittorrent.u

import akka.util.ByteString
import com.spooky.bittorrent.protocol.client.pwp.api.Unchoke
import com.spooky.bittorrent.protocol.client.pwp.api.Choke
import com.spooky.bittorrent.protocol.client.pwp.api.Cancel
import com.spooky.bittorrent.Showable

private sealed abstract class State
private object N extends State
private object CHOKE extends State
private object UNCHOKE extends State
class EventQueue {
  def Ordering = new Ordering[Tuple2[ByteString, Int]] {
    def compare(a: Tuple2[ByteString, Int], b: Tuple2[ByteString, Int]) = a._2.compare(b._2)
  }

  private var sequence: Long = 0
  private var state: State = N

  def enqueue(msg: ByteString): Unit = {
    val id = nextSequence
    val bucket = find(id)
    bucket.orElse(create(id)).map(_.set(id, msg))
  }

  def enqueue(msg: Showable): Unit = msg match {
    case c @ Choke   => transition(CHOKE)
    case u @ Unchoke => transition(UNCHOKE)
    case c: Cancel   => if (!remove(c)) enqueue(c.toByteString)
    case o           => enqueue(msg.toByteString)
  }

  def dequeue: ByteString = state match {
    case CHOKE => {
      state = N
      Choke.toByteString
    }
    case UNCHOKE => {
      state = N
      Unchoke.toByteString
    }
    case N => null
  }

  private def remove(c: Cancel): Boolean = false

  private def find(id: Long): Option[Bucket] = {
    ???
  }

  private def create(id: Long): Some[Bucket] = {
    ???
  }
  private def transition(r: State): Unit = state = r match {
    case CHOKE if state == UNCHOKE => N
    case UNCHOKE if state == CHOKE => N
    case c                         => c
  }

  private def nextSequence: Long = {
    sequence = sequence + 1
    sequence
  }
  private class Bucket(private var sequenceId: Long, size: Int) {
    private val content = new Array[ByteString](size)
    private def lastId: Long = {
      0
    }
    private def next(): Option[ByteString] = {
      None
    }
    private[u] def set(id: Long, value: ByteString): Unit = {

    }
    private def remove(c: Cancel): Boolean = {
      true
    }

  }

}
