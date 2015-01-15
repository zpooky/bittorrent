package com.spooky.bittorrent.protocol.client.pwp.api

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.model.PeerId
import akka.util.ByteString
import java.nio.ByteOrder
import java.nio.ByteBuffer
import com.spooky.bittorrent.metainfo.Sha1
import com.spooky.bittorrent.Binary
import java.util.BitSet
import java.nio.charset.Charset

trait Showable {
  def toByteString: ByteString
}
abstract class PeerWireMessage
case class Handshake(infoHash: Checksum, peerId: PeerId) extends PeerWireMessage with Showable {
  def toByteString: ByteString = {
    val buffer = ByteBuffer.allocate(1 + 19 + 8 + 20 + 20)
    buffer.put(19.asInstanceOf[Byte])
    val ascii = Charset.forName("ASCII")
    buffer.put("BitTorrent protocol".getBytes(ascii))
    buffer.put(infoHash.sum)
    buffer.put(peerId.id.getBytes(ascii))
    println(buffer)
    buffer.flip()
    println(buffer)
    ByteString(buffer)
  }
}
object Handshake {
  def apply(buffer: ByteBuffer): Handshake = {
    debug(buffer.duplicate())
    val length = buffer.get.asInstanceOf[Int] & 0xFF
    val p = read(buffer, length)
    val reserved = buffer.getLong
    val infoHash = Checksum(buffer, Sha1)
    val peerId = PeerId(read(buffer, 20))
    //    println(length + "|" + p + "|" + peerId)
    Handshake(infoHash, peerId)
  }
  private def debug(buffer: ByteBuffer) {
    buffer.order(ByteOrder.BIG_ENDIAN)
    val length = buffer.get.asInstanceOf[Int] & 0xFF
    println("pstrlen:" + length + "|pstr:" + read(buffer, length) + "|reserved:" + Binary.toBinary(buffer.getLong) + "|info_has:" + read(buffer, 20) + "|peer-id:" + read(buffer, 20))
    //    println(Binary.toBinary(length) + "|" + length + "|" + Binary.toBinary(length))
  }
  private def read(buffer: ByteBuffer, length: Int): String = {
    val builder = StringBuilder.newBuilder
    for (n <- 0 until length) {
      builder.append(buffer.get.asInstanceOf[Char])
    }
    builder.toString
  }
}
object PeerWireMessage {
  def apply(buffer: ByteBuffer): PeerWireMessage = {
    val dup = buffer.duplicate.get & 0xFF
    val length = buffer.getInt
    println("length::::::" + length + "|" + dup)
    if (length == 0) {
      KeepAlive
    } else {
      val messageId = buffer.get & 0xFF
      messageId match {
        case 0 => Choke
        case 1 => Unchoke
        case 2 => Intrested
        case 3 => NotIntrested
        case 4 => Have(buffer)
        case 5 => Bitfield(length - 1, buffer)
        case 6 => Request(buffer)
        case 7 => Piece(length - 1, buffer)
        case 8 => Cancel(buffer)
        case 9 => Port(buffer)
        case _ => null
      }
    }
  }
}
object KeepAlive extends PeerWireMessage
object Choke extends PeerWireMessage
object Unchoke extends PeerWireMessage
object Intrested extends PeerWireMessage
object NotIntrested extends PeerWireMessage
case class Have(index: Int) extends PeerWireMessage
case class Bitfield(blocks: BitSet) extends PeerWireMessage with Showable {
  def toByteString: ByteString = {
    null
  }
}
case class Request(index: Int, begin: Int, length: Int) extends PeerWireMessage
case class Piece(index: Int, begin: Int, buffer: ByteBuffer) extends PeerWireMessage
case class Cancel(index: Int, begin: Int, length: Int) extends PeerWireMessage
object Have {
  def apply(buffer: ByteBuffer): Have = Have(buffer.getInt)
}
object Bitfield {
  def apply(length: Int, buffer: ByteBuffer): Bitfield = {
    val limit = buffer.limit
    val bitfield = Bitfield(BitSet.valueOf(buffer.limit(length).asInstanceOf[ByteBuffer]))
    buffer.limit(limit)
    bitfield
  }
}
object Request {
  def apply(buffer: ByteBuffer): Request = Request(buffer.getInt, buffer.getInt, buffer.getInt)
}
object Piece {
  def apply(length: Int, buffer: ByteBuffer): Piece = {
    val index = buffer.getInt
    val begin = buffer.getInt
    Piece(index, begin, buffer.duplicate.limit(length - 8).asInstanceOf[ByteBuffer].compact)
  }
}
object Cancel {
  def apply(buffer: ByteBuffer): Cancel = Cancel(buffer.getInt, buffer.getInt, buffer.getInt)
}
case class Port(port: Int) extends PeerWireMessage
object Port {
  def apply(buffer: ByteBuffer): Port = Port(buffer.getShort & 0xFFFF)
}
