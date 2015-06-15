package com.spooky.bittorrent.metainfo

import com.spooky.bittorrent.Checksum
import com.spooky.bittorrent.InfoHash
import java.net.URL

case class Info(pieceLength: Int, length: Long, files: List[TorrentFile], pieces: List[Checksum], priv: Boolean, rootHash: Option[Checksum])
case class Node(host: String, port: Int)
case class Tracker(announce: String)
case class TorrentFile(name: String, bytes: Long)
case class Torrent(infoHash: InfoHash, info: Info, nodes: List[Node], trackers: Set[Tracker], creationDate: Option[String], comment: Option[String], createdBy: Option[String], encoding: Option[String], httpSeeds: List[URL]) extends Metainfo {
  //  override def toString: String = new GsonBuilder().setPrettyPrinting().create().toJson(this)
}
