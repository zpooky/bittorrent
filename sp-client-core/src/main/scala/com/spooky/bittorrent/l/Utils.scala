package com.spooky.bittorrent.l

import java.util.BitSet
import com.spooky.bittorrent.metainfo.Torrent
import java.security.MessageDigest
import scala.annotation.tailrec
import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.metainfo.Sha1
import java.nio.ByteBuffer
object Utils {

  class /*Not immutable*/ BitSetBuilder(bitSet: BitSet, index: Int) {

    def append(b: Boolean): BitSetBuilder = {
      bitSet.set(index, b)
      new BitSetBuilder(bitSet, index + 1)
    }
    def toBitSet: BitSet = {
      bitSet
    }
  }
  object BitSetBuilder {
    def apply(torrent: Torrent): BitSetBuilder = {
      println(torrent.info.length + "/" + torrent.info.pieceLength + "==" + Math.ceil(torrent.info.length / torrent.info.pieceLength))
      val capacity: Int = Math.ceil(torrent.info.length / torrent.info.pieceLength).asInstanceOf[Int]
      new BitSetBuilder(new BitSet(capacity), 0)
    }
  }
  case class Bûffer(bytes: Array[Byte], index: Int, capacity: Int) {
    def isEmpty: Boolean = if (index > capacity) throw new RuntimeException else index == capacity
    def consume(digest: MessageDigest, max: Int): Tuple2[Bûffer, Int /*Actualy read*/ ] = {
      val available = capacity - index
      val read = Math.min(max, available)
      digest.update(bytes, index, read)
      (copy(index = index + read), read)
    }
  }
  class /*Not immutable*/ BitSetConsumer(peiceLength: Int, bitSet: BitSetBuilder, digest: MessageDigest, current: Int, checksums: List[Checksum]) {
    def consume(bytes: Array[Byte]): BitSetConsumer = consume(bytes, bytes.length)
    def consume(bytes: ByteBuffer): BitSetConsumer = consume(bytes.array, bytes.limit)
    def consume(bytes: Array[Byte], size: Int): BitSetConsumer = {
        @tailrec
        def rec(bitSet: BitSetBuilder, digest: MessageDigest, buffer: Bûffer, current: Int, checksums: List[Checksum]): BitSetConsumer = {
          if (!buffer.isEmpty) {
            val requested = peiceLength - current
            val (bufferAfter, read) = buffer.consume(digest, requested)
            if (requested == read) {
              val d = digest.digest()
              val b = checksums.head.compare(d)
              rec(bitSet.append(b), Sha1.newMessageDigest, bufferAfter, 0, checksums.tail)
            } else {
              new BitSetConsumer(peiceLength, bitSet, digest, current + read, checksums)
            }
          } else {
            new BitSetConsumer(peiceLength, bitSet, digest, current, checksums)
          }
        }
      val buffer = Bûffer(bytes, 0, size)
      rec(bitSet, digest, buffer, current, checksums)
    }
    def toBitSet(): BitSet = {
      val builder = if (current != 0) {
        bitSet.append(checksums.head.compare(digest.digest()))
      } else bitSet
      builder.toBitSet
    }
  }
  object BitSetConsumer {
    def apply(torrent: Torrent): BitSetConsumer = {
      val peiceLength = torrent.info.pieceLength
      val capacity: Int = Math.ceil(torrent.info.length / peiceLength).asInstanceOf[Int]
      new BitSetConsumer(peiceLength, BitSetBuilder(torrent), Sha1.newMessageDigest, 0, torrent.info.pieces)
    }
  }
}
