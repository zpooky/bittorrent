package com.spooky.bittorrent.peer.tracker

import com.spooky.bittorrent.metainfo.Tracker
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.metainfo.Checksum
import spray.client.pipelining._
import spray.http.HttpRequest
import com.spooky.bittorrent.SpookyBittorrent
import scala.concurrent.Future
import spray.httpx.unmarshalling.Unmarshaller
import spray.http.HttpEntity
import spray.httpx.unmarshalling._
import com.spooky.bittorrent.bencode.Bencode
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import spray.http.MediaRanges._
import com.spooky.bittorrent.actors.BittorrentActors
import spray.httpx.UnsuccessfulResponseException
import scala.concurrent.duration._
import akka.util.Timeout

class TrackerManager(tracker: Tracker)(implicit context: ExecutionContext, actorSystem: ActorSystem, actors: BittorrentActors) {
  def announceEvent(info: Checksum)(implicit id: PeerId) {
    val requestTimeout = Timeout(1 millisecond)
    println("|" + tracker.announce)
    val pipeline = announcePipeline
    actors.announce ! Announcing(tracker)
    val result = pipeline(Get(tracker.announce))
    result.onSuccess({
      case (r: Announced) ⇒ actors.announce ! r.copy(tracker = Some(tracker))
    })
    result.onFailure({
      case (e: UnsuccessfulResponseException)                   ⇒ actors.announce ! ErrorResponse(e.getLocalizedMessage, 3 minutes, tracker)
      case (e: spray.can.Http.ConnectionAttemptFailedException) ⇒ actors.announce ! ErrorResponse(e.getLocalizedMessage, 1 hour, tracker)
      case (e: Exception)                                       ⇒ e.printStackTrace
    })
  }

  private implicit def AnnounceResponseUnmarshaller =
    Unmarshaller[Announced](`text/*`) {
      case HttpEntity.NonEmpty(_, data) ⇒ {
        Announced(Bencode.decode(data.toByteArray))
      }
    }

  private def announcePipeline: HttpRequest ⇒ Future[Announced] = (
    addHeader("User-Agent", SpookyBittorrent.userAgent)
    ~> sendReceive
    ~> unmarshal[Announced])
}