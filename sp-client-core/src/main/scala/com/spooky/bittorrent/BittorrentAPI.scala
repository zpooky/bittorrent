package com.spooky.bittorrent

import com.spooky.bittorrent.model.TorrentRef
import com.spooky.bittorrent.metainfo.Torrent
import akka.actor.ActorSystem
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.actors.BittorrentActors
import com.spooky.bittorrent.protocol.server.tracker.TrackerProvider
import com.spooky.bittorrent.protocol.server.tracker.TrackerManager
import com.spooky.bittorrent.SpookyBittorrent._
import com.spooky.bittorrent.model.TorrentStatistics
import com.spooky.bittorrent.l.SessionManager
import java.nio.file.Path
import com.spooky.bittorrent.l.file.FileInitiator
import com.spooky.bittorrent.model.TorrentSetup


object BittorrentAPI {
  implicit val system = ActorSystem("spooky-bittorrent")
  implicit val id = PeerId.create
  implicit val actors = new BittorrentActors(system)

  def start(setup: TorrentSetup): TorrentRef = {
    val torrent = setup.torrent
    val tracker = new TrackerProvider(torrent)
    val manager = new TrackerManager(torrent.trackers.find(_ => true).get)
    val statistics = TorrentStatistics(torrent.infoHash, 0, 0, 0, 0)
    val ref = SessionManager.register(setup)
    manager.announce(statistics)(id)
    ref
  }

  def stop(torrentRef: TorrentRef) {

  }
}
