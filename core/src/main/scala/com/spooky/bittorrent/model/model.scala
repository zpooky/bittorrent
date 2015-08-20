package com.spooky.bittorrent.model

import com.spooky.bittorrent.metainfo.Torrent
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.BitSet
import com.spooky.bittorrent.Binary
import java.nio.charset.Charset
import scala.annotation.tailrec
import com.spooky.bittorrent.Checksum
import com.spooky.bittorrent.InfoHash

case class PeerId(id: String)
object PeerId {
  val UTF8 = Charset.forName("UTF8")
  def parse(buffer: ByteBuffer): PeerId = {
    val buff = Array.ofDim[Byte](20)
    buffer.get(buff)
    //    val charset = Charset.forName("ASCII")
    PeerId(new String(buff, UTF8).intern())
  }
  def create = PeerId("SPOOKY6-c2b4f6c4h4d9")
}
case class TorrentFileState(have: BitSet) {
  //This is bugged since last piece is generaly not fully utalized
  def getDownloaded(torrent: Torrent): Long = {
      @tailrec
      def rec(bitset: BitSet, index: Int, accumulated: Long, length: Long): Long = {
        if (bitset.size == index) {
          accumulated
        } else rec(bitset, index + 1, if (bitset.get(index)) accumulated + length else accumulated, length)
      }
    rec(have, 0, 0l, torrent.info.pieceLength)
  }
  override def toString: String = {
    "|" + Binary.toBinary(have) + "|"
  }
}
case class TorrentSetup(torrent: Torrent, root: Path)
case class EnrichedTorrentSetup(torrent: Torrent, root: Path, state: TorrentFileState)
case class TorrentStatistics(infoHash: InfoHash, uploaded: Long, downloaded: Long, left: Long, corrupt: Long)
case class TorrentConfiguration(port: Short, numwant: Int)
abstract class AbstractPeer(ip: String, port: Short)
case class Peer(ip: String, port: Short) extends AbstractPeer(ip, port)

//rename to something maybe **Context
case class TorrentRef(info: InfoHash, peerId: PeerId)
object TorrentRef {
  def apply(torrent: Torrent, peerId: PeerId): TorrentRef = TorrentRef(torrent.infoHash, peerId)
}
