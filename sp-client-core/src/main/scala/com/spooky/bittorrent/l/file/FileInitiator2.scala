package com.spooky.bittorrent.l.file

import scala.collection.JavaConversions._
import com.spooky.bittorrent.l.Utils._
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Path
import com.spooky.bittorrent.metainfo.TorrentFile
import com.spooky.bittorrent.model.TorrentFileState
import scala.annotation.tailrec
import scala.reflect.io.File
import scala.io.Source
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.nio.ByteBuffer

class FileInitiator2(torrent: Torrent, root: Path) {
  private def rawFiles = torrent.info.files

  private def files: List[Tuple2[Path, Long]] = rawFiles.map({ case TorrentFile(file, bytes) => (root.resolve(file), bytes) }).toList
  //[23:47:11]  received bitfield, 6272 of 6274 pieces complete (99%)
  def state(): TorrentFileState = {
      @tailrec
      def rec(files: List[Tuple2[Path, Long]], consumer: BitSetConsumer): BitSetConsumer = files match {
        case Nil => consumer
        case (file, bytes) :: xs => {
          if (file.toFile.exists) {
            if (file.toFile.length == bytes) {
              rec(xs, verify(file, consumer))
            } else {
              throw new RuntimeException("Not implemented... sorry")
            }
          } else {
            throw new RuntimeException(s"stfu: file ${file} not found")
            //            val (bufferRest, lengthRest, checksumsRest, builderRest) = fill(File(file), bytes, checksums, builder, buffer, length)
            //            rec(xs, checksumsRest, builderRest, bufferRest, lengthRest)
          }
        }
      }

    val consumer = BitSetConsumer(torrent)
    val start = System.currentTimeMillis()
    val s = TorrentFileState(rec(files, consumer).toBitSet)
    println((((System.currentTimeMillis() - start)/1000d)/60d) + "min")
    s
  }
  //  implicit def convert(in: java.nio.file.Path): scala.reflect.io.Path = {
  //    scala.reflect.io.Path(in.toFile)
  //  }
  private def verify(file: Path, xx: BitSetConsumer): BitSetConsumer = {
      def rec(channel: FileChannel, consumer: BitSetConsumer, buffer: ByteBuffer, fileSize: Long, position: Long): BitSetConsumer = {
//        assert(position == channel.position)
        if (position == fileSize) {
          channel.close()
          consumer
        } else {
          channel.read(buffer)
          val newPositon = position + buffer.position
          rec(channel, consumer.consume(buffer.flip().asInstanceOf[ByteBuffer]), buffer.clear.asInstanceOf[ByteBuffer], fileSize, newPositon)
        }
      }
    val buffer = ByteBuffer.allocate(1024 * 4)
    val channel = FileChannel.open(file, StandardOpenOption.READ)
    rec(channel, xx, buffer, channel.size, 0)
  }
}
