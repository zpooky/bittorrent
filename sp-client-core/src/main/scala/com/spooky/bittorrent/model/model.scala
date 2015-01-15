package com.spooky.bittorrent.model

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.bencode.BString
import com.spooky.bittorrent.bencode.BList
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.ByteBuffer
import java.nio.file.Path
import scala.collection.mutable.BitSet

case class PeerId(id: String)
object PeerId {
  def apply() = {}
  def create = PeerId("SPOOKY6-c2b4f6c4h4d9")
}
case class TorrentFileState(have: BitSet)
case class TorrentSetup(torrent: Torrent, root: Path)
case class TorrentStatistics(infoHash: Checksum, uploaded: Long, downloaded: Long, left: Long, corrupt: Long)
case class TorrentConfiguration(port: Short, numwant: Int)
abstract class AbstractPeer(ip: String, port: Short)
case class Peer(ip: String, port: Short) extends AbstractPeer(ip, port)
case class TorrentRef(info: Checksum, peerId: PeerId)
object TorrentRef {
  def apply(torrent: Torrent, peerId: PeerId): TorrentRef = TorrentRef(torrent.infoHash, peerId)
}
