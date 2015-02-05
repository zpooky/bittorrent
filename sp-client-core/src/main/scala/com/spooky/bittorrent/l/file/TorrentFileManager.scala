package com.spooky.bittorrent.l.file

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Path
import java.util.BitSet
import java.nio.ByteBuffer
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption._
import scala.collection.Map
import com.spooky.bittorrent.metainfo.TorrentFile
import scala.annotation.tailrec
import java.nio.Buffer
import com.spooky.bittorrent.model.TorrentFileState
import java.util.concurrent.atomic.AtomicReference
import com.spooky.bittorrent.metainfo.TorrentFile
import java.nio.file.StandardOpenOption
//import scala.collection.mutable.Map

object TorrentFileManager {
  def apply(torrent: Torrent, root: Path, state: TorrentFileState): TorrentFileManager = new TorrentFileManager(torrent, root, new AtomicReference[BitSet](state.have))
}

class TorrentFileManager private (torrent: Torrent, root: Path, have: AtomicReference[BitSet]) {
  private lazy val channels = toChannels(torrent).toMap

  private def toChannels(torrent: Torrent): List[Tuple2[Path, FileChannel]] = {
      @tailrec
      def rec(files: List[TorrentFile], result: List[Tuple2[Path, FileChannel]]): List[Tuple2[Path, FileChannel]] = files match {
        case Nil => result
        case TorrentFile(filename, _) :: xs => {
          val filePath = toAbsolute(filename)
          val channel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE)
          rec(xs, (filePath, channel) :: result)
        }
      }
    rec(torrent.info.files, Nil)
  }
  def have(index: Int): Boolean = {
    checkBounds(index)
    have.get.get(index)
  }
  def haveAnyBlocks: Boolean = {
      def rec(set: BitSet, index: Int): Boolean = {
        if (set.size == index) {
          false
        } else if (set.get(index)) {
          true
        } else {
          rec(set, index + 1)
        }
      }
    rec(have.get, 0)
  }
  def blocks: BitSet = have.get
  def read(index: Int, begin: Int, length: Int): ByteBuffer = {
    val buffer = ByteBuffer.wrap(Array.ofDim[Byte](length))
    fileFor(index, begin, length).foreach({
      case Tuple3(path, offset, length) => {
        val channel = channels.get(path).get
        //fills to buffer from offset. or if buffer is greater then channel content read till channel is empty
        channel.read(buffer, offset)
      }
    })
    buffer.flip.asInstanceOf[ByteBuffer]
  }
  def write(index: Int, begin: Int, data: ByteBuffer): Unit = {
    checkIndexConstraints(begin, data.limit())
    fileFor(index, begin, data.limit).foreach({
      case Tuple3(path, offset, length) => {
        val writer = channels.get(path).get.position(offset)
        val lock = writer.lock(offset, length, false)
        try {
          writer.write(data, length)
        } finally {
          lock.release
        }
      }
    })
  }
  private def checkBounds(index: Int) {

  }
  private def checkIndexConstraints(begin: Int, length: Long): Unit = if (begin + length > blockSize) {
    println("wtgf")
  }
  private def startOf(index: Int, begin: Int): Long = (index * blockSize) + begin
  private[file] def toAbsolute(file: String): Path = root.resolve(file)
  private def blockSize: Int = torrent.info.pieceLength
  private def get(): RandomAccessFile = {
    null
  }
  type Offset = Long
  type Length = Long
  def fileFor(index: Int, begin: Int, length: Int): List[Tuple3[Path, Offset, Length]] = {
      //          @tailrec
      def rec(start: Long, length: Long, files: List[TorrentFile]): List[Tuple3[Path, Offset, Length]] = files match {
        case Nil if length > 0          => throw new RuntimeException
        case Nil                        => Nil
        case _ if length == 0           => Nil
        case s :: xs if start < s.bytes => (toAbsolute(s.name), start, if (length > s.bytes) s.bytes else length) :: rec(0, (start + length) - s.bytes, xs)
        case s :: xs                    => rec(start - s.bytes, length, xs)
      }
    val start = startOf(index, begin)
    rec(start, length, torrent.info.files)
  }
  def close() {
    channels.values.foreach { value => value.close() }
  }
}
