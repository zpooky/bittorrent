package com.spooky.bittorrent.l.file

import org.scalatest.FunSuite
import java.io.File
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Paths
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.nio.ByteBuffer
import java.security.MessageDigest

class TorrentFileManager_SingleFileTest extends FunSuite {
  val file = new File(TorrentFileManager.getClass.getResource("/debian.torrent").toURI)
  val torrent = Torrent(file)
  val root = Paths.get("P:\\tmp\\t")
  lazy val stat = new FileInitiator2(torrent, root).state()

  val pieceLength = torrent.info.pieceLength

  test("the same") {
    val fm = TorrentFileManager(torrent, root, stat)
    val buffer1 = fm.read(0, 0, pieceLength)
    val buffer2 = fm.read(0, 0, pieceLength)
    assert(buffer1.equals(buffer2))
    fm.close()
  }
  test("indices") {
    val fm = TorrentFileManager(torrent, root, stat)
    val channel = FileChannel.open(fm.toAbsolute(torrent.info.files.head.name), StandardOpenOption.READ)
    val channelBuffer = ByteBuffer.allocate(pieceLength)
    for (index <- 0 until torrent.info.pieces.length) {
      val buffer = fm.read(index, 0, pieceLength)
      channel.read(channelBuffer)
      assert(buffer.equals(channelBuffer.flip()))
    }
    channel.close
    fm.close()
  }

  test("request 16kb chunks and verify checksum. chunks will allign up evenly piece(pieceLengt % 16kb == 0)") {
    val kbs = 1024 * 16
    verifyWith(kbs)
  }

  test("request 17kb chunks and verify checksum. chunks will not allign up evenly piece(pieceLengt % 17kb == 2)") {
    val kbs = 1024 * 17
    verifyWith(kbs)
  }
  def verifyWith(chunkSize: Int) {
    val fm = TorrentFileManager(torrent, root, stat)
    var checksum = torrent.info.pieces
    for (index <- 0 until torrent.info.pieces.length) {
      val digest = MessageDigest.getInstance("sha1")
      for (offset <- 0 until (pieceLength, chunkSize)) {
        val buffer = fm.read(index, offset, chunkSize)
        digest.update(buffer)
      }
      assert(checksum.head.compare(digest.digest()))
      checksum = checksum.tail
    }
    assert(checksum == Nil)
    fm.close()
  }
}

class TorrentFileManager_MultipleFileTest extends FunSuite{

}
