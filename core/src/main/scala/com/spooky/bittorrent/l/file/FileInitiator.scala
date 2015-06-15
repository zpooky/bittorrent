package com.spooky.bittorrent.l.file

import scala.collection.JavaConversions._
import com.spooky.bittorrent.l.Utils._
import com.spooky.bittorrent.metainfo.Torrent
import com.spooky.bittorrent.metainfo.TorrentFile
import com.spooky.bittorrent.model.TorrentFileState
import java.nio.file.Path
import java.nio.channels.FileChannel
import scala.reflect.io.File
import java.io.BufferedInputStream
import scala.annotation.tailrec
import java.io.BufferedWriter
import com.spooky.bittorrent.Checksum

class FileInitiator(torrent: Torrent, root: Path) {
  private def rawFiles = torrent.info.files

  private def files: List[Tuple2[Path, Long]] = rawFiles.map({ case TorrentFile(file, bytes) => (root.resolve(file), bytes) }).toList

  def state(): TorrentFileState = {
      @tailrec
      def rec(files: List[Tuple2[Path, Long]], checksums: List[Checksum], builder: BitSetBuilder, buffer: Array[Byte], length: Int): BitSetBuilder = files match {
        case Nil => builder
        case s :: xs => {
          val (file, bytes) = s
          if (file.exists) {
            if (file.toFile.length == bytes) {
              val (bufferRest, lengthRest, checksumsRest, builderRest) = verify(File(file), checksums, builder, buffer, length)
              rec(xs, checksumsRest, builderRest, bufferRest, lengthRest)
            } else {
              throw new RuntimeException("Not implemented... sorry")
            }
          } else {
            //            val (bufferRest, lengthRest, checksumsRest, builderRest) = fill(File(file), bytes, checksums, builder, buffer, length)
            //            rec(xs, checksumsRest, builderRest, bufferRest, lengthRest)
            throw new RuntimeException("not yet")
          }
        }
      }
    TorrentFileState(rec(files, torrent.info.pieces, BitSetBuilder(torrent), Array.ofDim[Byte](torrent.info.pieceLength), 0).toBitSet)
  }
  implicit def convert(in: java.nio.file.Path): scala.reflect.io.Path = {
    scala.reflect.io.Path(in.toFile)
  }
  private def verify(file: File, pieces: List[Checksum], builder: BitSetBuilder, buffer: Array[Byte], length: Int): Tuple4[Array[Byte], Int, List[Checksum], BitSetBuilder] = {
      @tailrec
      def rec(reader: BufferedInputStream, bytes: Long, buffer: Array[Byte], pieces: List[Checksum], builder: BitSetBuilder, startLength: Int = 0): Tuple4[Array[Byte], Int, List[Checksum], BitSetBuilder] = pieces match {
        case Nil => {
          reader.close()
          (buffer, startLength, Nil, builder)
        }
        case l @ (s :: xs) => {
          val size = reader.read(buffer, startLength, buffer.length - startLength)
          println(size + "|" + (size + startLength) + "|" + (buffer.length))
          if (size + startLength == buffer.length) {
            rec(reader, bytes - size, buffer, xs, builder.append(s.check(buffer)))
          } else {
            reader.close()
            (buffer, size + startLength, l, builder)
          }
        }
      }
    rec(file.bufferedInput(), file.length, buffer, pieces, builder, length)
  }

  private def fill(file: File, bytes: Long, pieces: List[Checksum], builder: BitSetBuilder, buffer: Array[Byte], length: Int): Tuple4[Array[Byte], Int, List[Checksum], BitSetBuilder] = {
      @tailrec
      def recx(stream: BufferedWriter, bytes: Long, pieces: List[Checksum], buffer: Array[Byte], builder: BitSetBuilder, length: Int = 0): Tuple4[Array[Byte], Int, List[Checksum], BitSetBuilder] = pieces match {
        case xs if bytes == 0 => {
          stream.close
          (buffer, length, xs, builder)
        }
        case Nil => throw new RuntimeException("the checksums should cover the whole file.(should never run out of checksums when zeroing a file, sinze the files are specified in the torrent)")
        case l @ (s :: xs) =>
          val byte = 0.asInstanceOf[Byte]
          val size = Math.min(bytes, buffer.length).toInt
          if (size != 0) {
            for (index <- length to (length + size)) {
              buffer(index) = byte
              stream.write(byte)
            }
            recx(stream, bytes - size, xs, buffer, builder.append(s.check(buffer)))
          } else {
            stream.close
            (buffer, length, l, builder)
          }
      }
    val stream = file.bufferedWriter(false)
    recx(stream, bytes, pieces, buffer, builder, length)
  }
}
