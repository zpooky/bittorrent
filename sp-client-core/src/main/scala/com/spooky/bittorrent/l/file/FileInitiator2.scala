package com.spooky.bittorrent.l.file

import scala.collection.JavaConversions._
import com.spooky.bittorrent.l.Utils._
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Path
import com.spooky.bittorrent.metainfo.TorrentFile
import com.spooky.bittorrent.model.TorrentFileState
import scala.annotation.tailrec
import com.spooky.bittorrent.metainfo.Checksum
import scala.reflect.io.File
import scala.io.Source
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.nio.ByteBuffer

class FileInitiator2(torrent: Torrent, root: Path) {
  private def rawFiles = torrent.info.files

  private def files: List[Tuple2[Path, Long]] = rawFiles.map({ case TorrentFile(file, bytes) => (root.resolve(file), bytes) }).toList

  def state(): TorrentFileState = {
      @tailrec
      def rec(files: List[Tuple2[Path, Long]], consumer: BitSetConsumer): BitSetConsumer = files match {
        case Nil => consumer
        case s :: xs => {
          val (file, bytes) = s
          if (file.toFile.exists) {
            if (file.toFile.length == bytes) {
              val consumerAfter = verify(file, consumer)
              rec(xs, consumerAfter)
            } else {
              throw new RuntimeException("Not implemented... sorry")
            }
          } else {
            throw new RuntimeException("stfu")
            //            val (bufferRest, lengthRest, checksumsRest, builderRest) = fill(File(file), bytes, checksums, builder, buffer, length)
            //            rec(xs, checksumsRest, builderRest, bufferRest, lengthRest)
          }
        }
      }
    val consumer = BitSetConsumer(torrent)
    TorrentFileState(rec(files, consumer).toBitSet)
  }
  //  implicit def convert(in: java.nio.file.Path): scala.reflect.io.Path = {
  //    scala.reflect.io.Path(in.toFile)
  //  }
  private def verify(file: Path, xx: BitSetConsumer): BitSetConsumer = {
      def rec(channel: FileChannel, consumer: BitSetConsumer, buffer: ByteBuffer): BitSetConsumer = {
        if (channel.position == channel.size) {
          channel.close()
          consumer
        } else {
          channel.read(buffer)
          rec(channel, consumer.consume(buffer.flip().asInstanceOf[ByteBuffer]), buffer.clear.asInstanceOf[ByteBuffer])
        }
      }
    val buffer = ByteBuffer.allocate(1024)
    val channel = FileChannel.open(file, StandardOpenOption.READ)
    rec(channel, xx, buffer)
  }
}
