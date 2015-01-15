package com.spooky.bittorrent.l.file

import scala.collection.JavaConversions._
import com.spooky.bittorrent.metainfo.Torrent
import java.nio.file.Path
import com.spooky.bittorrent.metainfo.TorrentFile
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption._
import scala.io.Source
import scala.reflect.io.File
import com.spooky.bittorrent.model.TorrentFileState

object FileInitiator {
  def apply(torrent: Torrent, root: Path): TorrentFileState = {
    val files = torrent.info.files
    files.map({ case TorrentFile(file, bytes) => (root.resolve(file), bytes) }).foreach({
      case Tuple2(file, bytes) => {
       if(file.exists) {
//         verify()
       }else {
         fill(file, bytes)
       }
      }
    })
    TorrentFileState(null)
  }
  implicit def convert(in: java.nio.file.Path): scala.reflect.io.Path = {
    scala.reflect.io.Path(in.toFile)
  }
  private def fill(filePath: Path, bytes: Long) {//TODO check that if existing file exceeds "bytes"
    val stream = File(filePath).bufferedWriter(false)
    try {
      val byte = 0.asInstanceOf[Byte]
      for (_ <- 1l to bytes) {
        stream.write(byte)
      }
    } finally {
      stream.close
    }
  }
}
