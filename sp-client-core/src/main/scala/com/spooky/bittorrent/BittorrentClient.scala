package com.spooky.bittorrent

import com.spooky.bittorrent.peer.CompositePeerProvider
import com.spooky.bittorrent.metainfo.Torrent
import java.io.File
import com.spooky.bittorrent.model.PeerId
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import com.spooky.bittorrent.peer.tracker.TrackerManager
import com.spooky.bittorrent.peer.tracker.TrackerProvider
import com.spooky.bittorrent.actors.BittorrentActors

object BittorrentClient {
  def main(args: Array[String]) {
    //http://192.168.0.110:2710/announce
    //    val provider = CompositePeerProvider(Torrent(new File("")), Nil)
    //    val peers = provider.get(5)
    implicit val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
    implicit val system = ActorSystem("spooky-bittorrent")
    implicit val id = PeerId.create
    implicit val actors = new BittorrentActors(system)
    val torrent = Torrent(new File("D:\\torrent\\Community.S05E01.HDTV.x264-LOL.mp4.torrent"))
    val tracker = new TrackerProvider(torrent)
    val manager = new TrackerManager(torrent.trackers.find(_ ⇒ true).get)
    manager.announceEvent(torrent.infoHash)(id)
    //ref = Client.start(torrent)
  }
}