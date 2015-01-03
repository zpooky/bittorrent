package com.spooky.bittorrent.peer.tracker

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.bencode.BValue
import com.spooky.bittorrent.model.PeerId
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import com.spooky.bittorrent.metainfo.Tracker
import scala.concurrent.duration.FiniteDuration

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
case class Announced(s: String,
                     dateTime: LocalDateTime = LocalDateTime.now,
                     tracker: Option[Tracker])
object Announced {
  def apply(value: BValue): Announced = {
    null
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

case class TrackerPeer(peerId: Option[PeerId], ip: String, port: Short)
object TrackerPeer {
  def apply(value: BValue): TrackerPeer = null
  def apply(value: Array[Byte]): TrackerPeer = {
    val buffer = ByteBuffer.wrap(value)
    buffer.order(ByteOrder.BIG_ENDIAN)
    val ip = Range(0, 3).map(i ⇒ buffer.get.asInstanceOf[Int]).foldLeft(StringBuilder.newBuilder)((builder, current) ⇒ builder.append(current)).toString
    val port = buffer.getShort
    TrackerPeer(None, ip, port)
  }
}