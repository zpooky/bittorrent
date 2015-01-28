package com.spooky.bittorrent.protocol.client.pwp.api

import org.scalatest.FunSuite
import java.util.BitSet
import java.nio.ByteBuffer
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.metainfo.Sha1
import com.spooky.bittorrent.ImmutableByteBuffer

class RequestTest extends FunSuite {
  def getBitSet = BitSet.valueOf(Array(Integer.MAX_VALUE.asInstanceOf[Byte], 0.asInstanceOf[Byte], Integer.MIN_VALUE.asInstanceOf[Byte], 1.asInstanceOf[Byte], 33.asInstanceOf[Byte], 44.asInstanceOf[Byte]))
  def getByteBuffer: ByteBuffer = {
    val buffer = ByteBuffer.allocate(8)
    val l: Long = -6148914691236517206L
    buffer.putLong(l)
    buffer.flip().asInstanceOf[ByteBuffer]
  }

  def getChecksum: Checksum = {
    val raw = ByteBuffer.allocate(20)
    val l: Long = -6148914691236517206L
    raw.putLong(l)
    raw.putLong(l)
    raw.putInt(-1431655766)
    Checksum.parse(raw.flip().asInstanceOf[ByteBuffer], Sha1)
  }
  def getByteId: PeerId = {
    PeerId("09876543211234567890")
  }
  test("Bitfield") {
    val bitset = getBitSet
    val before = Bitfield(bitset)
    val after = PeerWireMessage(before.toByteBuffer)
    assert(before.equals(after))
  }
  test("KeepAlive") {
    PeerWireMessage(KeepAlive.toByteBuffer) match {
      case KeepAlive =>
      case _         => fail()
    }
  }
  test("Handshake") {
    val message = Handshake(getChecksum, getByteId)
    assert(message == Handshake.parse(message.toByteBuffer))
  }
  test("Choke") {
    PeerWireMessage(Choke.toByteBuffer) match {
      case Choke =>
      case _     => fail()
    }
  }
  test("Unchoke") {
    PeerWireMessage(Unchoke.toByteBuffer) match {
      case Unchoke =>
      case _       => fail()
    }
  }
  test("Intrested") {
    PeerWireMessage(Intrested.toByteBuffer) match {
      case Intrested =>
      case _         => fail()
    }
  }
  test("NotIntrested") {
    PeerWireMessage(NotIntrested.toByteBuffer) match {
      case NotIntrested =>
      case _            => fail()
    }
  }
  test("Have") {
    val message = Have(1337)
    PeerWireMessage(message.toByteBuffer) match {
      case Have(1337) =>
      case _          => fail()
    }

  }
  test("Request") {
    val message = Request(1337, Integer.MAX_VALUE, Integer.MIN_VALUE)
    PeerWireMessage(message.toByteBuffer) match {
      case Request(1337, Integer.MAX_VALUE, Integer.MIN_VALUE) =>
      case _ => fail()
    }
  }
  test("Piece") {
    val message = Piece(1337, 0, ImmutableByteBuffer(getByteBuffer))
    val after = PeerWireMessage(message.toByteBuffer)
    assert(message.equals(after))
  }
  test("Cancel") {
    val message = Cancel(4444, 77, 7323)
    PeerWireMessage(message.toByteBuffer) match {
      case Cancel(4444, 77, 7323) =>
      case _                      => fail()
    }
  }
  test("Port") {
    val message = Port(25555)
    PeerWireMessage(message.toByteBuffer) match {
      case Port(25555) =>
      case _           => fail()
    }
  }
}
