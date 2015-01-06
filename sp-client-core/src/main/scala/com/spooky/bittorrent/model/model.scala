package com.spooky.bittorrent.model

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.bencode.BString
import com.spooky.bittorrent.bencode.BList
import com.spooky.bittorrent.metainfo.Torrent

case class PeerId(id: String)
object PeerId {
  def create = PeerId("SPOOKY6-c2b4f6c4h4d9")
}

case class TorrentStatistics(infoHash: Checksum, uploaded: Long, downloaded: Long, left: Long, corrupt: Long)
case class TorrentConfiguration(port: Short, numwant: Int)
abstract class AbstractPeer(ip: String, port: Short)
case class Peer(ip: String, port: Short) extends AbstractPeer(ip, port)
case class TorrentRef(info: Checksum)
object TorrentRef {
  def apply(torrent: Torrent): TorrentRef = TorrentRef(torrent.infoHash)
}