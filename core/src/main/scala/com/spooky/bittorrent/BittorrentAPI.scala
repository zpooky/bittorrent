package com.spooky.bittorrent

import com.spooky.bittorrent.model.TorrentRef
import com.spooky.bittorrent.metainfo.Torrent
import spooky.actor.ActorSystem
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.actors.BittorrentActors
import com.spooky.bittorrent.protocol.server.tracker.TrackerProvider
import com.spooky.bittorrent.protocol.server.tracker.TrackerManager
import com.spooky.bittorrent.SpookyBittorrent._
import com.spooky.bittorrent.model.TorrentStatistics
import com.spooky.bittorrent.l.session.SessionManager
import java.nio.file.Path
import com.spooky.bittorrent.l.file.FileInitiator
import com.spooky.bittorrent.model.TorrentSetup
import com.spooky.bittorrent.l.file.FileInitiator2
//import com.typesafe.config.ConfigFactory
import java.io.File

object BittorrentAPI {
  //  private val config = ConfigFactory.parseFile(new File(BittorrentAPI.getClass.getResource("/application.conf").toURI))
  private implicit lazy val system = ActorSystem("spooky-bittorrent" /*, config*/ )
  private implicit val id = PeerId.create
  private implicit val actors = new BittorrentActors(system)
  //TODO make it better(message passing to actors and the like)
  def start(setup: TorrentSetup): TorrentRef = {
    val torrent = setup.torrent
    //    val tracker = new TrackerProvider(torrent)
    //    val manager = new TrackerManager(torrent.trackers.find(_ => true).get)
    val statistics = TorrentStatistics(torrent.infoHash, torrent.info.length, torrent.info.length, 0, 0)
    //    val ref = SessionManager.register(setup)
    //    manager.announce(statistics)(id)
    actors.start ! setup
    TorrentRef(torrent, id)
  }

  def stop(torrentRef: TorrentRef) {

  }
}
