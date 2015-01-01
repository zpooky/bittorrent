package com.spooky.bittorrent.bencode

import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.nio.channels.FileChannel.MapMode
import java.nio.ByteBuffer
import scala.Range

class TorrentFileStream(channel: FileChannel, buffer: ByteBuffer) extends BStream{
  def headChar: Char = buffer.duplicate.get.asInstanceOf[Char]
  def headByte: Byte =  buffer.duplicate.get
  def tail = {
    val tail = buffer.duplicate
    tail.get
    new TorrentFileStream(channel,tail)
  }
  def isEmpty = !buffer.hasRemaining
  def close: Unit = channel.close
}
object TorrentFileStream {
  def apply(torrent:File) = {
    val channel = FileChannel.open(torrent.toPath, StandardOpenOption.READ)
    new TorrentFileStream(channel,channel.map(MapMode.READ_ONLY, 0, channel.size).load)
  }
  def main(args: Array[String]): Unit = {
    val url = getClass.getResource("/debian.torrent")
    val file = new File(url.toURI())
    var parser = TorrentFileStream(file)
    val d = Bencode.decode(parser)
    println(d)
    parser.close
  }
    def printBinary(b1: Byte) = {
      String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
  }
}