package com.spooky.bittorrent

import com.spooky.bittorrent.metainfo.Torrent
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Paths
import com.spooky.bittorrent.model.TorrentSetup
import org.apache.commons.codec.binary.Hex
import com.spooky.bittorrent.metainfo.Torrents

object BittorrentClient {
  def main(args: Array[String]) {
    //http://192.168.0.110:2710/announce
    //    val provider = CompositePeerProvider(Torrent(new File("")), Nil)
    //    val peers = provider.get(5)
//    val file = new File(BittorrentClient.getClass.getResource("/local-debian.torrent").toURI)
    val file = new File(BittorrentClient.getClass.getResource("/debian.torrent").toURI)
    val torrent = Torrents(file)
    println("infoHash:"+torrent.infoHash)
    val setup = TorrentSetup(torrent, Paths.get("P:\\tmp\\t"))
    val ref = BittorrentAPI.start(setup)

    println(ManagementFactory.getRuntimeMXBean().getName())
    //ref = Client.start(torrent)
    //    Thread.sleep(5000)
    //    System.exit(0)

  }
}


//tixati-Request 16 kb chunks
