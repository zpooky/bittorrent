package com.spooky.bittorrent.l.file

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

class TorrentFileManager private (val torrent: Torrent, root: Path, have: AtomicReference[BitSet]) {
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
  def complete: Boolean = {
      @tailrec
      def rec(set: BitSet, index: Int): Boolean = {
        if (set.size == index) {
          true
        } else if (!set.get(index)) {
          false
        } else {
          rec(set, index + 1)
        }
      }
    rec(have.get, 0)
  }
  def haveAnyBlocks: Boolean = {
      @tailrec
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

  //java.nio.HeapByteBuffer[pos=0 lim=16384 cap=16384] path:O:\tmp\file.dump,offset:-1199570944,length:16384|index:1762,begin:0,
  //def---(1762--------0-----------16384
  def read(index: Int, begin: Int, length: Int): ByteBuffer = {
    val buffer = ByteBuffer.allocate(length)
    val files = fileFor(index, begin, length)
    files.foreach({
      //------|-1199570944|16384
      case (path, offset, length) => {
        val channel = channels.get(path).get
        //fills to buffer from offset. or if buffer is greater then channel content read till channel is empty
        try {
          channel.read(buffer, offset)
        } catch {
          case e: Exception => throw new Exception(s"${buffer} path:${path},offset:${offset},length:${length}|index:${index},begin:${begin},|||${torrent.info.files}")
        }
      }
    })
    buffer.flip.asInstanceOf[ByteBuffer]
  }
  def write(index: Int, begin: Int, data: ByteBuffer): Unit = {
    checkIndexConstraints(begin, data.limit())
    fileFor(index, begin, data.limit).foreach({
      case (path, offset, length) => {
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
  private def startOf(index: Long, begin: Long): Long = (index * blockSize) + begin
  private[file] def toAbsolute(file: String): Path = root.resolve(file)
  private def blockSize: Int = torrent.info.pieceLength
  private def get(): RandomAccessFile = {
    null
  }
  type Offset = Long
  type Length = Long
  //def----(1762--------0-----------16384
  def fileFor(index: Int, begin: Int, length: Int): List[Tuple3[Path, Offset, Length]] = {
      //          @tailrec
      def rec(start: Long, toRead: Long, files: List[TorrentFile]): List[Tuple3[Path, Offset, Length]] = files match {
        case Nil if toRead > 0 => throw new RuntimeException
        case Nil               => Nil
        case _ if toRead <= 0  => Nil
        case file :: xs if start < file.bytes => {
          val head = (toAbsolute(file.name), start, Math.min(file.bytes, toRead))
          if(toRead < 0 || head._2 < 0 || head._3 < 0){
            println()
          }
          head :: rec(0, (start + toRead) - file.bytes, xs)
        }
        case file :: xs => rec(start - file.bytes, toRead, xs)
      }
    val start = startOf(index, begin)
    rec(start, length, torrent.info.files)
  }
  def close() {
    channels.values.foreach { value => value.close() }
  }
}
