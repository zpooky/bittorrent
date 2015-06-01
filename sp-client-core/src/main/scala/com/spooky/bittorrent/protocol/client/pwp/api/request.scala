package com.spooky.bittorrent.protocol.client.pwp.api

import com.spooky.bittorrent.model.PeerId
import akka.util.ByteString
import java.nio.ByteOrder
import java.nio.ByteBuffer
import com.spooky.bittorrent.Binary
import java.util.BitSet
import java.nio.charset.Charset
import java.security.MessageDigest
import com.spooky.bittorrent.ImmutableByteBuffer
import com.spooky.bittorrent.Checksum
import com.spooky.bittorrent.Sha1
import scala.annotation.tailrec
import com.spooky.bittorrent.InfoHash

trait Showable {
  def toByteBuffer: ByteBuffer
  def toByteString: ByteString = ByteString(toByteBuffer)
}
abstract class PeerWireMessage
case class Handshake(infoHash: InfoHash, peerId: PeerId) extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(1 + 19 + 8 + 20 + 20).order(ByteOrder.BIG_ENDIAN)
    buffer.put(19.asInstanceOf[Byte])
    val ascii = Charset.forName("ASCII")
    buffer.put("BitTorrent protocol".getBytes(ascii))
    buffer.putLong(0)
    buffer.put(infoHash.sum)
    buffer.put(peerId.id.getBytes(ascii))
    buffer.flip().asInstanceOf[ByteBuffer]
  }
  override def equals(other: Any): Boolean = other match {
    case Handshake(otherHash: InfoHash, PeerId(otherId)) => otherHash == infoHash && otherId == peerId

    case _ => false
  }
}
object Handshake {
  def parse(buffer: ByteBuffer): Handshake = {
    //    debug(buffer.duplicate())
    val length = buffer.get.asInstanceOf[Int] & 0xFF
    val p = read(buffer, length)
    val reserved = buffer.getLong
    val infoHash = InfoHash.parse(buffer)
    val peerId = PeerId.parse(buffer)
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
  def parse(buffer: ByteBuffer): List[PeerWireMessage] = {
    if (buffer.hasRemaining()) {
      apply(buffer) match {
        case Some(e) => e :: parse(buffer)
        case None    => Nil
      }
    } else Nil
  }

  def apply(buffer: ByteBuffer): Option[PeerWireMessage] = {
    if (!buffer.hasRemaining()) {
      None
    } else {
      val dup = buffer.duplicate.get & 0xFF
      val length = buffer.getInt
        //      println("length::::::" + length + "|" + dup)
        implicit def toOption[T](any: T): Option[T] = Option(any)
      if (length == 0) {
        KeepAlive
      } else {
        val messageId = buffer.get & 0xFF
        messageId match {
          case 0 => Choke
          case 1 => Unchoke
          case 2 => Intrested
          case 3 => NotIntrested
          case 4 => Have.parse(buffer)
          case 5 => Bitfield.parse(length - 1, buffer)
          case 6 => Request.parse(buffer)
          case 7 => Piece.parse(length - 1, buffer)
          case 8 => Cancel.parse(buffer)
          case 9 => Port.parse(buffer)
          case _ => null
        }
      }
    }
  }

}
//keep-alive: <len=0000>
object KeepAlive extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(0)
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//choke: <len=0001><id=0>
object Choke extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(1)
    buffer.put(0.asInstanceOf[Byte])
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//unchoke: <len=0001><id=1>
object Unchoke extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(1)
    buffer.put(1.asInstanceOf[Byte])
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//interested: <len=0001><id=2>
object Intrested extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(1)
    buffer.put(2.asInstanceOf[Byte])
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//not interested: <len=0001><id=3>
object NotIntrested extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(1)
    buffer.put(3.asInstanceOf[Byte])
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//have: <len=0005><id=4><piece index>
case class Have(index: Int) extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1 + 4).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(5)
    buffer.put(4.asInstanceOf[Byte])
    buffer.putInt(index)
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//bitfield: <len=0001+X><id=5><bitfield>
case class Bitfield(blocks: BitSet) extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    //TODO optimize
    val bitsetArr = blocks.toByteArray()
    val buffer = ByteBuffer.allocate(4 + 1 + bitsetArr.length).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(bitsetArr.length + 1)
    buffer.put(5.asInstanceOf[Byte])
    buffer.put(bitsetArr)
    val b = buffer.flip().asInstanceOf[ByteBuffer]
    b
  }
}
//request: <len=0013><id=6><index><begin><length>
case class Request(index: Int, begin: Int, length: Int) extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1 + 4 + 4 + 4).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(13)
    buffer.put(6.asInstanceOf[Byte])
    buffer.putInt(index)
    buffer.putInt(begin)
    buffer.putInt(length)
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//piece: <len=0009+X><id=7><index><begin><block>
case class Piece(index: Int, begin: Int, block: ImmutableByteBuffer) extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val capacity = 4 + 1 + 4 + 4 + block.length
    val buffer = ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(capacity - 4)
    buffer.put(7.asInstanceOf[Byte])
    buffer.putInt(index)
    buffer.putInt(begin)
    buffer.put(block.toByteBuffer)
    buffer.flip().asInstanceOf[ByteBuffer]
  }
  override def equals(other: Any): Boolean = other match {
    case Piece(oIndex, oBegin, oBlock) => {
      index == oIndex && begin == oBegin && block == oBlock
    }
    case _ => false
  }
}
//cancel: <len=0013><id=8><index><begin><length>
case class Cancel(index: Int, begin: Int, length: Int) extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1 + 4 + 4 + 4).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(13)
    buffer.put(8.asInstanceOf[Byte])
    buffer.putInt(index)
    buffer.putInt(begin)
    buffer.putInt(length)
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
//port: <len=0003><id=9><listen-port>
case class Port(port: Int) extends PeerWireMessage with Showable {
  def toByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(4 + 1 + 4).order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(3)
    buffer.put(9.asInstanceOf[Byte])
    buffer.putShort(port.asInstanceOf[Short])
    buffer.flip().asInstanceOf[ByteBuffer]
  }
}
object Have {
  protected[api] def parse(buffer: ByteBuffer): Have = Have(buffer.getInt)
}
object Bitfield {
  private[api] def parse(length: Int, buffer: ByteBuffer): Bitfield = {
    val limit = buffer.limit
    val bitfield = try {
      Bitfield(BitSet.valueOf(buffer.duplicate().limit(buffer.position + length).asInstanceOf[ByteBuffer]))
    } catch {
      case e: IllegalArgumentException => {
        println(buffer.position() + "|" + length + "|" + (buffer.position + length) + "|" + buffer)
        throw e
      }
    }
    buffer.position(limit)
    bitfield
  }
}
object Request {
  private[api] def parse(buffer: ByteBuffer): Request = Request(buffer.getInt, buffer.getInt, buffer.getInt)
}
object Piece {
  private[api] def parse(length: Int, buffer: ByteBuffer): Piece = {
    val index = buffer.getInt
    val begin = buffer.getInt
    val block = Array.ofDim[Byte](length - 8)
    buffer.get(block)
    //TODO do something if block is not filled
    Piece(index, begin, ImmutableByteBuffer(block))
  }
}
object Cancel {
  private[api] def parse(buffer: ByteBuffer): Cancel = Cancel(buffer.getInt, buffer.getInt, buffer.getInt)
}

object Port {
  private[api] def parse(buffer: ByteBuffer): Port = Port(buffer.getShort & 0xFFFF)
}
