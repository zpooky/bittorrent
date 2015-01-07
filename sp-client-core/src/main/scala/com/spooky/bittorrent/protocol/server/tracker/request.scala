package com.spooky.bittorrent.protocol.server.tracker

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.bencode.BValue
import com.spooky.bittorrent.model.PeerId
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import com.spooky.bittorrent.metainfo.Tracker
import scala.concurrent.duration.FiniteDuration
import com.spooky.bittorrent.bencode.BValue
import com.spooky.bittorrent.bencode.BList
import com.spooky.bittorrent.bencode.BDictionary
import java.math.BigInteger
import com.spooky.bittorrent.bencode.BInteger
import com.spooky.bittorrent.bencode.BString
import com.spooky.bittorrent.model.AbstractPeer
import com.spooky.bittorrent.bencode.BValue
import com.spooky.bittorrent.bencode.BDictionary
import scala.annotation.tailrec
import com.spooky.bittorrent.bencode.BString

case class Announcing(tracker: Tracker)
case class AnnounceRequest(checkSum: Checksum,
                           port: Int,
                           uploaded: Long,
                           downloaded: Long,
                           left: Long,
                           corrupt: Long,
                           key: String,
                           numwant: Int,
                           compact: Boolean,
                           noPeerId: Int)
abstract class Announced(dateTime: LocalDateTime, tracker: Option[Tracker])
case class SuccessAnnounced(warningMessage: Option[String],
                            minInterval: Option[FiniteDuration],
                            interval: FiniteDuration,
                            incomplete: Int,
                            complete: Int,
                            trackerId: Option[String],
                            peers: List[TrackerPeer],
                            dateTime: LocalDateTime = LocalDateTime.now,
                            tracker: Option[Tracker] = None) extends Announced(dateTime, tracker)
case class FailureAnnounced(reason: String, dateTime: LocalDateTime = LocalDateTime.now, tracker: Option[Tracker] = None) extends Announced(dateTime, tracker)
object Announced {
  def apply(value: BValue): Announced = value match {
    case (dict: BDictionary) => {
      val oFailure = dict.get("failure reason")
      if (oFailure.isDefined) {
        FailureAnnounced(oFailure.map { case (s: BString) => s.value }.get)
      } else {
        val peers = getPeers(dict.get("peers"))
        import scala.concurrent.duration._
        val warningMessage = dict.get("warning message").map { case (s: BString) => s.value }
        val minInterval = dict.get("min interval").map { case (i: BInteger) => i.value seconds }
        val interval = dict.get("interval").map { case (i: BInteger) => i.value seconds }.get
        val incomplete = dict.get("incomplete").map({ case (i: BInteger) => i.value }).getOrElse(0l)
        val complete = dict.get("complete").map({ case (i: BInteger) => i.value }).getOrElse(0l)
        val trackerId = dict.get("tracker id").map { case (s: BString) => s.value }
        SuccessAnnounced(warningMessage, minInterval, interval, incomplete.toInt, complete.toInt, trackerId, peers)
      }
    }
    case _ => throw new RuntimeException
  }
  private def getPeers(dict: Option[BValue]): List[TrackerPeer] = dict match {
    case Some((s: BString)) => TrackerPeer(s)
    case Some((l: BList))   => TrackerPeer(l)
    case None               => Nil
    case _                  => throw new RuntimeException
  }
}
abstract class Error(retry: FiniteDuration, dateTime: LocalDateTime, tracker: Tracker)
case class TimeoutResponse(retry: FiniteDuration, tracker: Tracker, dateTime: LocalDateTime = LocalDateTime.now) extends Error(retry, dateTime, tracker)
case class ErrorResponse(message: String, retry: FiniteDuration, tracker: Tracker, dateTime: LocalDateTime = LocalDateTime.now) extends Error(retry, dateTime, tracker)

case class TrackerRequest(s: String)
case class TrackerResponse(failureReason: Option[String],
                           warningReason: Option[String],
                           interval: Int,
                           minInterval: Option[Int],
                           trackerId: Option[String],
                           complete: Int,
                           incomplete: Int,
                           peers: List[TrackerPeer],
                           dateTime: LocalDateTime = LocalDateTime.now)

case class TrackerPeer(peerId: Option[PeerId], ip: String, port: Short) extends AbstractPeer(ip, port)
object TrackerPeer {
  def apply(list: BList): List[TrackerPeer] = list match {
    case BList(l) => TrackerPeer(l, List())
  }
  @tailrec
  private def apply(list: List[BValue], result: List[TrackerPeer]): List[TrackerPeer] = list match {
    case head :: tail => head match {
      case (d: BDictionary) => TrackerPeer(tail, TrackerPeer(d) :: result)
      case _                => throw new RuntimeException
    }
    case Nil => result
  }

  def apply(str: BString): List[TrackerPeer] = {
    val value = str.value.getBytes
    val buffer = ByteBuffer.wrap(value)
    if (buffer.hasRemaining) {
      xx(buffer, List())
    } else Nil
  }
  @tailrec
  private def xx(buffer: ByteBuffer, result: List[TrackerPeer]): List[TrackerPeer] = {
    buffer.order(ByteOrder.BIG_ENDIAN)
    val ip = Range(0, 4).map(i => buffer.get.asInstanceOf[Int] & 0xFF).foldLeft(StringBuilder.newBuilder)((builder, current) => builder.append(current).append(".")).toString
    val port = buffer.getShort
    if (buffer.hasRemaining) {
      xx(buffer, TrackerPeer(None, ip, port) :: result)
    } else {
      result
    }
  }

  def apply(dict: BDictionary): TrackerPeer = {
    val id = dict.get("peer id").map { case (s: BString) => PeerId(s.value) }
    val ip = dict.get("ip").map { case (s: BString) => s.value }.get
    val port = dict.get("port").map { case (i: BInteger) => i.value.toShort }.get
    TrackerPeer(id, ip, port)
  }
}
