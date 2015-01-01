package com.spooky.bittorrent.metainfo

import java.io.File
import scala.io.Source
import scala.io.Codec
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.nio.channels.FileChannel.MapMode
import com.spooky.bittorrent.bencode.TorrentFileStream

case class Torrent(info: Checksum, trackers: List[Tracker], files: List[TorrentFile], creationDate: Option[String], comment: Option[String], createdBy: Option[String], encoding: Option[String]) //extends Metainfo
object Torrent {
  def apply(torrent: File) = {
    val parser = TorrentFileStream(torrent)
    null
  }
}