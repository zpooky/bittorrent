package com.spooky.bittorrent.actors

import akka.actor.Actor
import com.spooky.bittorrent.model.TorrentRef
import com.spooky.bittorrent.peer.tracker.TrackerManager
import com.spooky.bittorrent.metainfo.Torrent
import com.spooky.bittorrent.SpookyBittorrent._
import com.spooky.bittorrent.peer.CompositePeerProvider

case class StartRequest(torrent: Torrent)
class TorrentStartActor(actors: BittorrentActors) extends Actor {
  def receive = {
    case StartRequest(torrent) ⇒ {
      CompositePeerProvider(torrent,Nil)
      sender() ! new TorrentRef(null)
    }
  }
}