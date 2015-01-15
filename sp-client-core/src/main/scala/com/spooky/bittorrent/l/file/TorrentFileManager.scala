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
//import scala.collection.mutable.Map

class TorrentFileManager(torrent: Torrent, root: Path) {
  private lazy val writers = Map[Path, FileChannel]()

  def have(index: Int): Boolean = false
  def haveAnyBlocks: Boolean = false
  def blocks: BitSet = null
  def read(index: Int, begin: Int, length: Int): String = null
  def write(index: Int, begin: Int, data: ByteBuffer): Unit = {
      def checkBounds(index: Int, position: Long, length: Int) {
        //        FileChannel.open(file, WRITE,CREATE)
      }
      def getPosition(index: Int, begin: Int): Long = {
        0l
      }
      def fileFor(index: Int, begin: Int, length: Int): List[Tuple2[Path, Long]] = {
          @tailrec
          def rec(start: Long, length: Long, files: List[TorrentFile]): List[Tuple2[Path, Long]] = files match {
            case Nil if length > 0          => throw new RuntimeException
            case Nil                        => Nil
            case _ if length == 0           => Nil
            case x :: xs if start < x.bytes => (toAbsolute(x.name), start) :: rec(0, (start + length) - x.bytes, xs)
            case x :: xs                    => rec(start - x.bytes, length, xs)
          }
        val i: Long = (index * blockSize) + begin
        rec(i, length, torrent.info.files)
      }
    //    val position = getPosition(index, begin)
    //    val length = data.limit
    //    checkBounds(index, position, length)
    fileFor(index, begin, data.limit).foreach({
      case Tuple2(path, offset) => {
        val writer = writers.get(path).get.position(offset)
        writer.lock(offset, length, false)
        try {
          writer.write(data)
        } finally {
          writer.lock.release
        }
      }
    })
  }
  private def toAbsolute(file: String): Path = root.resolve(file)
  private def blockSize: Int = torrent.info.pieceLength
  def close() {
    writers.values.foreach { value => value.close() }
  }
}
