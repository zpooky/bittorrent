package com.spooky.bittorrent

import com.spooky.bittorrent.metainfo.Torrent
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Paths
import com.spooky.bittorrent.model.TorrentSetup
import org.apache.commons.codec.binary.Hex
import com.spooky.bittorrent.metainfo.Torrents
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import java.io.PrintWriter
import java.io.PrintStream

object BittorrentClient {
  def main(args: Array[String]) {
    setupLog
    //http://192.168.0.110:2710/announce
    //    val provider = CompositePeerProvider(Torrent(new File("")), Nil)
    //    val peers = provider.get(5)
    //    val file = new File(BittorrentClient.getClass.getResource("/local-debian.torrent").toURI)

    //    val setup = debian()
    //    		val setup = random()
    val api = BittorrentAPI
    api.start(debian())
    api.start(random())

    println(ManagementFactory.getRuntimeMXBean().getName())
    //ref = Client.start(torrent)
    //    Thread.sleep(5000)
    //    System.exit(0)

  }

  def debian() = {
	  println(BittorrentClient.getClass.getResource("/debian.torrent"))
	  println(BittorrentClient.getClass.getResource("/debian.torrent").toURI)
    val file = new File(BittorrentClient.getClass.getResource("/debian.torrent").toURI)
    println(file)
    val torrent = Torrents(file)
    println("infoHash:" + torrent.infoHash)
    TorrentSetup(torrent, Paths.get("P:\\tmp\\t"))
  }
  def random() = {
    val file = new File("O:\\tmp\\file.dump.torrent")
    val torrent = Torrents(file)
    println("infoHash:" + torrent.infoHash)
    TorrentSetup(torrent, Paths.get("O:\\tmp\\"))
  }

  def setupLog = {
    println(s"log root: ${getRoot}")
    val err = new File(getRoot, s"err-${logTimestamp}.log")
    System.setErr(new PrintStream(err))
    val out = new File(getRoot, s"out-${logTimestamp}.log")
    System.setOut(new PrintStream(out))
  }

  def getRoot: File = {
    val root = new File(".").getAbsoluteFile
    val log = new File(root, "log")
    if (!log.isDirectory) {
      log.mkdirs
    }
    log
  }

  def logTimestamp = {
    val now = DateTime.now
    val fmt = DateTimeFormat.forPattern("yyyy-dd-MM-HH-mm-ss");
    fmt.print(now);
  }
}


//tixati-Request 16 kb chunks
