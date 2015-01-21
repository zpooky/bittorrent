package com.spooky.bittorrent.l.file

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Path
import java.util.BitSet
import java.nio.ByteBuffer
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption._
import scala.collection.concurrent.Map
import com.spooky.bittorrent.metainfo.TorrentFile
import scala.annotation.tailrec
import java.nio.Buffer
//import scala.collection.mutable.Map

class TorrentFileManager(torrent: Torrent, root: Path) {
  private lazy val channels = Map[Path, FileChannel]()
  //  {
  //      def apply(key: Path): Option[FileChannel] = {
  //        null
  //      }
  //  }

  def have(index: Int): Boolean = false
  def haveAnyBlocks: Boolean = false
  val b = 0.asInstanceOf[Byte]
  def blocks: BitSet = BitSet.valueOf(Array.fill[Byte](Math.ceil(torrent.info.length / torrent.info.pieceLength).asInstanceOf[Int])(0))
  def read(index: Int, begin: Int, length: Int): ByteBuffer = {
    val buffer = ByteBuffer.wrap(Array.ofDim[Byte](length))
    fileFor(index, begin, length).foreach({
      case Tuple3(path, offset, length) => {
        val readers = channels.get(path).get
//        readers.transferTo(offset, length, buffer)
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
  private def checkIndexConstraints(begin: Int, length: Long): Unit = if (begin + length > blockSize) {
    println("wtgf")
  }
  private def startOf(index: Int, begin: Int): Long = (index * blockSize) + begin
  private def toAbsolute(file: String): Path = root.resolve(file)
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
