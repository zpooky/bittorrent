package com.spooky.bittorrent.l.file

import org.scalatest.FunSuite
import java.io.File
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Paths
import java.security.MessageDigest
import java.nio.channels.FileChannel
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.nio.file.Path
import java.nio.ByteBuffer
import com.spooky.bittorrent.metainfo.Torrents

class FileInitiatorTest extends FunSuite {
  private def getMultiFileTorrent = {
    val file = new File(getClass.getResource("/helix.torrent").toURI)
//    val file = new File(getClass.getResource("/the.game.torrent").toURI)
    Torrents(file)
  }
  private def getSingleFileTorrent = {
    val file = new File(getClass.getResource("/debian.torrent").toURI)
    Torrents(file)
  }
  private def getRoot: String = {
    "O:\\Downloads\\"
  }
  ignore("FileInitiator") {
    val torrent = getMultiFileTorrent
    val initator = new FileInitiator(torrent, Paths.get(getRoot))
    println(initator.state())
  }
  test("FileInitiator2-multi-file") {
    val torrent = getMultiFileTorrent
    val initator = new FileInitiator2(torrent, Paths.get(getRoot))
    println(initator.state())
  }
  ignore("FileInitiator2-single-file") {
    val torrent = getSingleFileTorrent
    println(torrent.toString)
    val initator = new FileInitiator2(torrent, Paths.get(getRoot))
    println(initator.state())
  }
  ignore("xxxx") {
    val torrent = getMultiFileTorrent
    val piece = torrent.info.pieceLength
    println(piece)

    //------------
    val first = torrent.info.files(0)
    val buff1 = ByteBuffer.allocate(first.bytes.asInstanceOf[Int]);
    {
      println(first)
      val firstChannel = FileChannel.open(Paths.get(getRoot, first.name), StandardOpenOption.READ)
      firstChannel.read(buff1)
      firstChannel.close
      val digester = MessageDigest.getInstance("sha1")
      digester.update(buff1.duplicate().flip().asInstanceOf[ByteBuffer])
      println("===" + MessageDigest.isEqual(torrent.info.pieces(0).sum, digester.digest()))
    } //------------
    val buff2 = ByteBuffer.allocate(torrent.info.pieceLength - first.bytes.asInstanceOf[Int]);
    {
      val second = torrent.info.files(1)
      println(second)
      val secondChannel = FileChannel.open(Paths.get(getRoot, second.name), StandardOpenOption.READ)
      secondChannel.read(buff2)
      secondChannel.close
    }
    //------------
    println(buff1.duplicate().flip())
    println(buff2.duplicate().flip())
    val digester = MessageDigest.getInstance("sha1")
    digester.update(buff1.flip().asInstanceOf[ByteBuffer])
    digester.update(buff2.flip().asInstanceOf[ByteBuffer])
    println("!!!!" + MessageDigest.isEqual(torrent.info.pieces(0).sum, digester.digest()))
  }
}
