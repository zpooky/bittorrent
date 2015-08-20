package com.spooky.bittorrent.protocol.server.tracker

import spooky.actor.ActorSystem
import com.spooky.bittorrent.metainfo.Tracker
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.SpookyBittorrent
import com.spooky.bittorrent.actors.BittorrentActors
import com.spooky.bittorrent.model.TorrentStatistics
import com.spooky.bittorrent.model.TorrentConfiguration
import com.spooky.bittorrent.Config
import com.spooky.bittorrent.protocol.server.tracker._
import com.spooky.bencode.Bencode
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.apache.commons.codec.net.URLCodec
import java.net.URL
import java.nio.charset.Charset
import java.util.Base64

import dispatch._, Defaults._

class TrackerManager(tracker: Tracker)( /*implicit context: ExecutionContext, actorSystem: ActorSystem,*/ actors: BittorrentActors) {
  protected val UTF8 = Charset.forName("UTF8")
  def announce(statistics: TorrentStatistics)(implicit id: PeerId) {
    //    val requestTimeout = Timeout(1 millisecond)
    //    val pipeline = announcePipeline
    actors.announce ! Announcing(tracker)
    val config = TorrentConfiguration(2555, 200)
    println(urlx(tracker.announce, config, statistics, id))
    val request = urlx(tracker.announce, config, statistics, id)
    val result = Http(request OK as.Bytes)
    //    val result = pipeline(Get(url(tracker.announce, config, statistics, id)))
    result.onSuccess({
      case (data: Array[Byte]) => {
        val b = Bencode.decode(data)
        println(b)
        Announced(b) match {
          case r: SuccessAnnounced => actors.announce ! r.copy(tracker = Some(tracker))
          case r: FailureAnnounced => actors.announce ! r.copy(tracker = Some(tracker))
          case e => println(s"unknown: $e")
        }
      }
    })
    result.onFailure({
      //      case (e: UnsuccessfulResponseException)                   => actors.announce ! ErrorResponse(e.getLocalizedMessage, 3 minutes, tracker)
      //      case (e: spray.can.Http.ConnectionAttemptFailedException) => actors.announce ! ErrorResponse(e.getLocalizedMessage, 1 hour, tracker)
      case (e: Exception) => e.printStackTrace
    })
  }

  private def urlx(base: String, configuration: TorrentConfiguration, statistics: TorrentStatistics, id: PeerId) = {
    val codec = new URLCodec
    val ascii = Charset.forName("ASCII")
    //    val encoder = Base64.getEncoder()
    val infoHash = new String(codec.encode(statistics.infoHash.raw), ascii)
    val peerId = id.id
    val key = "ss"
    val port = 6881
//    val builder = StringBuilder.newBuilder
//    builder ++= base
//    builder ++= s"info_hash=${infoHash}&peer_id=${peerId}&no_peer_id=0&event=started&port=${Config.peerWireProtocolPort}&"
//    builder ++= s"uploaded=${statistics.uploaded}&downloaded=${statistics.downloaded}&left=${statistics.left}&"
//    builder ++= s"corrupt=${statistics.corrupt}&key=${key}&numwant=${configuration.numwant}&compact=1"
//    println(builder)

    url(base) //
      .addQueryParameter("info_hash", infoHash) //
      .addQueryParameter("peer_id", peerId) //
      .addQueryParameter("no_peer_id", "0") //
      .addQueryParameter("event", "started") //
      .addQueryParameter("port", s"${Config.peerWireProtocolPort}") //
      .addQueryParameter("uploaded", s"${statistics.uploaded}") //
      .addQueryParameter("downloaded", s"${statistics.downloaded}") //
      .addQueryParameter("left", s"${statistics.left}") //
      .addQueryParameter("corrupt", s"${statistics.corrupt}") //
      .addQueryParameter("key", key) //
      .addQueryParameter("numwant", s"${configuration.numwant}") //
      .addQueryParameter("compact", "1") //
      .setHeader("User-Agent", SpookyBittorrent.userAgent) //

    //    val query = Uri.Query.apply(builder.toString, ascii, ParsingMode.RelaxedWithRawQuery)
    //    Uri(url).withQuery(query)
  }

  //  private implicit def AnnounceResponseUnmarshaller =
  //    Unmarshaller[Announced](MediaRanges.`*/*`) {
  //      case HttpEntity.NonEmpty(_, data) => {
  //        val b = Bencodes.decode(data.toByteArray)
  //        Announced(b)
  ////        Announced(null)
  //      }
  //      case e => println(e); null
  //    }

  //  private def announcePipeline: HttpRequest => Future[Announced] = (
  //    addHeader("User-Agent", SpookyBittorrent.userAgent)
  //    ~> sendReceive
  //    ~> unmarshal[Announced])
}
