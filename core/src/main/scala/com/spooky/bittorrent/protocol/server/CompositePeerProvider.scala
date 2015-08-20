package com.spooky.bittorrent.protocol.server

import com.spooky.bittorrent.metainfo.Metainfo
import com.spooky.bittorrent.protocol.server.tracker.TrackerManager
import com.spooky.bittorrent.metainfo.Tracker
import com.spooky.bittorrent.metainfo.Torrent
import com.spooky.bittorrent.actors.BittorrentActors

object CompositePeerProvider {
  def apply(metaInfo: Metainfo): CompositePeerProvider = new CompositePeerProvider(metaInfo, Set.empty)
  def apply(torrent: Torrent): CompositePeerProvider = new CompositePeerProvider(torrent, torrent.trackers)
}

class CompositePeerProvider(implicit val actors: BittorrentActors)(metaInfo: Metainfo, trackers: Set[Tracker]) {

  private val t = trackers.map(t => new TrackerManager(t))


//  val manager = new TrackerManager(torrent.trackers.find(_ => true).get)

  //  def get(count: Int): List[Peer] = null
}

