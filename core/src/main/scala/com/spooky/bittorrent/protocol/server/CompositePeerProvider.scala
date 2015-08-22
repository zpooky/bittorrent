package com.spooky.bittorrent.protocol.server

import com.spooky.bittorrent.metainfo.Metainfo
import com.spooky.bittorrent.protocol.server.tracker.TrackerManager
import com.spooky.bittorrent.metainfo.Tracker
import com.spooky.bittorrent.metainfo.Torrent
import com.spooky.bittorrent.actors.BittorrentActors
import com.spooky.bittorrent.model.TorrentStatistics
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.l.session.Session

object CompositePeerProvider {
  def apply(metaInfo: Metainfo, session: Session): CompositePeerProvider = new CompositePeerProvider(session, metaInfo, Set.empty)
  def apply(torrent: Torrent, session: Session): CompositePeerProvider = new CompositePeerProvider(session, torrent, torrent.trackers)
}

class CompositePeerProvider(session: Session, metaInfo: Metainfo, trackers: Set[Tracker]) {
  private val t = trackers.map(t => new TrackerManager(session)(t))
  private val statistics = session.statistics
  t.foreach(_.announce(statistics, session.peerId))
  //  val manager = new TrackerManager(torrent.trackers.find(_ => true).get)

  //  def get(count: Int): List[Peer] = null
}

