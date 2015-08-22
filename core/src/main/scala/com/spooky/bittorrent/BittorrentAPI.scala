package com.spooky.bittorrent

import spooky.actor.ActorSystem
import com.spooky.bittorrent.model.TorrentRef
import com.spooky.bittorrent.metainfo.Torrent
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.actors.BittorrentActors
import com.spooky.bittorrent.model.TorrentStatistics
import com.spooky.bittorrent.model.TorrentSetup

object BittorrentAPI {
  //  private val config = ConfigFactory.parseFile(new File(BittorrentAPI.getClass.getResource("/application.conf").toURI))
  private implicit lazy val system = ActorSystem("spooky-bittorrent" /*, config*/ )
  private implicit val id = PeerId.create
  private implicit val actors = new BittorrentActors(system)
  //TODO make it better(message passing to actors and the like)
  def start(setup: TorrentSetup): TorrentRef = {
    actors.start ! setup
    TorrentRef(setup.torrent, id)
  }

  def stop(torrentRef: TorrentRef): Unit = {

  }
}
