package com.spooky.bittorrent.bencode

import scala.collection.JavaConversions._
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.nio.channels.FileChannel.MapMode
import java.nio.ByteBuffer
import scala.Range

class TorrentFileStream(channel: FileChannel, buffer: ByteBuffer) extends BStream {
  def headChar: Char = buffer.duplicate.get.asInstanceOf[Char]
  def headByte: Byte = buffer.duplicate.get
  def tail = {
    val tail = buffer.duplicate
    tail.get
    new TorrentFileStream(channel, tail)
  }
  def isEmpty = !buffer.hasRemaining
  def close: Unit = channel.close
  override def toString: String = {
    val buff = buffer.duplicate
    val builder = StringBuilder.newBuilder
    while (buff.hasRemaining) {
      if (builder.endsWith("6:pieces")) {
        val bah = StringBuilder.newBuilder
        var chaaa = buff.get.asInstanceOf[Char]
        while ("0123456789".contains(chaaa)) {
          bah.append(chaaa)
          chaaa = buff.get.asInstanceOf[Char]
        }
        var i = bah.toString.toInt
        while(i >= 0){
          buff.get
          i = i-1
        }
      }
      builder += buff.get.asInstanceOf[Char]
    }
    builder.toString
  }
}
object TorrentFileStream {
  def apply(torrent: File) = {
    val channel = FileChannel.open(torrent.toPath, StandardOpenOption.READ)
    new TorrentFileStream(channel, channel.map(MapMode.READ_ONLY, 0, channel.size).load)
  }

  def printBinary(b1: Byte) = {
    String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
  }
}