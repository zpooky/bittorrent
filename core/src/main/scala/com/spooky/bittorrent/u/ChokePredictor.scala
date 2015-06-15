package com.spooky.bittorrent.u

import com.spooky.bittorrent.protocol.client.pwp.api.Request
import com.spooky.bittorrent.protocol.client.pwp.api.Piece
import com.spooky.bittorrent.l.session.client.ClientSession
import com.spooky.bittorrent.Showable

sealed case class AbortPlaceholder(length: Int)
sealed abstract class Thing
object Thing {
  def apply(s: Showable): Thing = s match {
    case p: Piece => PieceThing(p.block.length)
    case _        => NoThing
  }
}
case class PieceThing(length: Int) extends Thing
object NoThing extends Thing
class ChokePredictor(window: Size, session: ClientSession) {
  private[u] var outstanding: Long = 0
  private var max: Long = 0
  //Write buffer(server) - incomming
  def incomming(l: Any): Unit = l match {
    case r: Request => update(r.length)
    case _          =>
  }

  //Write buffer(server) - outgoing
  def outgoing(l: Thing): Unit = l match {
    case PieceThing(length) => update(-length)
    case _                  =>
  }

  def outgoing(l: AbortPlaceholder): Unit = {
    update(-l.length)
  }

  private def update(l: Long): Unit = {
    outstanding = outstanding + l
    max = Math.max(max, outstanding)
//    if(outstanding <= 0){
//      println(max)
//      max = 0
//    }
    tick
  }

  private def tick: Unit = {
    if (shouldChoke) {
      session.choke()
    } else if (shouldUnchoke) {
      session.unchoke()
    }
  }
  private def shouldChoke: Boolean = window.percentageFor(Size(outstanding, Byte)) > 90
  private def shouldUnchoke: Boolean = window.percentageFor(Size(outstanding, Byte)) < 60

}
