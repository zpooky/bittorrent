package com.spooky.bittorrent.l.file

import org.scalatest.FunSuite
import java.io.File
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Paths

class FileInitiatorTest extends FunSuite {
  test("") {
    val file = new File(getClass.getResource("/the.game.torrent").toURI)
    val torrent = Torrent(file)
    val initator = new FileInitiator(torrent, Paths.get("O:\\Downloads\\"))
    println(initator.state())
  }
}
