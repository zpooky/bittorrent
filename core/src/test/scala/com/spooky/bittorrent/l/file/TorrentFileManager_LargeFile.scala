package com.spooky.bittorrent.l.file

import org.scalatest.FunSuite
import java.io.File
import com.spooky.bittorrent.metainfo.Torrents
import java.nio.file.Paths

class TorrentFileManagerTest_LargeFile extends FunSuite {
  val file = new File("O:\\tmp\\file.dump.torrent")
  val torrent = Torrents(file)
  val root = Paths.get("O:\\tmp")
  lazy val stat = new FileInitiator2(torrent, root).state()

  test("xx") {
    val fm = TorrentFileManager(torrent, root, stat)
  }

}
object TorrentFileManagerTest_LargeFilex {
  val file = new File("O:\\tmp\\file.dump.torrent")
  val torrent = Torrents(file)
  val root = Paths.get("O:\\tmp")
  lazy val stat = new FileInitiator2(torrent, root).state()
  def main(args: Array[String]) {
    val fm = TorrentFileManager(torrent, root, stat)
    assert(fm.complete)
  }
}
