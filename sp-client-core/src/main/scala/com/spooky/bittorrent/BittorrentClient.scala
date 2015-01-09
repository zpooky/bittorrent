package com.spooky.bittorrent

import com.spooky.bittorrent.metainfo.Torrent
import java.io.File
import com.spooky.bittorrent.model.PeerId
import akka.actor.ActorSystem
import com.spooky.bittorrent.actors.BittorrentActors
import java.lang.management.ManagementFactory
import com.spooky.bittorrent.model.TorrentStatistics
import com.spooky.bittorrent.SpookyBittorrent._
import com.spooky.bittorrent.protocol.server.tracker.TrackerProvider
import com.spooky.bittorrent.protocol.server.tracker.TrackerManager
import java.nio.ByteBuffer

object BittorrentClient {
  def main(args: Array[String]) {
    //http://192.168.0.110:2710/announce
    //    val provider = CompositePeerProvider(Torrent(new File("")), Nil)
    //    val peers = provider.get(5)
    implicit val system = ActorSystem("spooky-bittorrent")
    implicit val id = PeerId.create
    implicit val actors = new BittorrentActors(system)
    val file = new File(BittorrentClient.getClass.getResource("/debian.torrent").toURI)
    //val file =new File("D:\\torrent\\Community.S05E01.HDTV.x264-LOL.mp4.torrent")
    val torrent = Torrent(file)
    val tracker = new TrackerProvider(torrent)
    val manager = new TrackerManager(torrent.trackers.find(_ => true).get)
    val statistics = TorrentStatistics(torrent.infoHash, 0, 0, 0, 0)
    manager.announceEvent(statistics)(id)
    println(ManagementFactory.getRuntimeMXBean().getName())
    //ref = Client.start(torrent)
    //    Thread.sleep(5000)
    //    System.exit(0)
    val test = ByteBuffer.allocate(11).putInt(0)// == Keepalive

  }
}
