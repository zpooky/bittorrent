package com.spooky.bittorrent.torrent

import com.spooky.bittorrent.metainfo.Info
import java.nio.file.Path
import com.spooky.bittorrent.metainfo.TorrentFile
import com.spooky.bittorrent.Checksum
import java.nio.ByteBuffer

class TorrentFileCreator {
  def info(files: List[Path], pieceLength: Int, privateTorrent: Boolean, merkleTree: Boolean): Info = {
    val tFiles = torrentFiles(files)
    val cs = checksums(files, Nil, ByteBuffer.allocateDirect(1024 * 4))
    val l = length(files)

    Info(pieceLength = pieceLength, length = l, files = tFiles.map(_._2), pieces = cs, priv = privateTorrent, rootHash = None)
  }

  private def length(files: List[Path]): Long = files match {
    case Nil       => 0
    case (x :: xs) => x.toFile.length + length(xs)
  }

  private def checksums(files: List[Path], result: List[Checksum], read: ByteBuffer): List[Checksum] = files match {
    case Nil       => result
    case (x :: xs) => Nil
  }

  private def torrentFiles(files: List[Path]): List[Tuple2[Path, TorrentFile]] = {
    ???
  }
}