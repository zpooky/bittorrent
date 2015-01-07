package com.spooky.bittorrent.bencode

import com.spooky.bittorrent.metainfo.Checksum
import java.security.MessageDigest
import com.spooky.bittorrent.bencode.wtf._
import com.spooky.bittorrent.metainfo.Sha1
import scala.annotation.tailrec

object InfoHash {
  val bencode = Bencode
  def hash(stream: BStream): Checksum = {
    val hasher = MessageDigest.getInstance("sha1")
    val info = subInfo(stream)
    val digest = hasher.digest(info)
    Checksum(digest, Sha1)
  }
  @tailrec
  private def subInfo(stream: BStream): Array[Byte] = stream match {
    case 'd' %:: tail => subInfo(tail)
    case c %:: _ if c.isDigit => {
      val key = bencode.decodeString(stream)
      key._2 match {
        case BString("info") => consume(key._1)
        case _               => subInfo(bencode.doDecode(key._1)._1)
      }
    }
    case head %:: tail => throw new RuntimeException
  }
  private def consume(stream: BStream): Array[Byte] = stream match {
    case 'd' %:: tail => decodeDictionary(tail, BytesBuilder(stream.headByte))._2.toArray
    case _            => throw new RuntimeException("info value is not a dictionary")
  }
  private def decodeDictionary(stream: BStream, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = {
    @tailrec
    def decode(stream: BStream, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = stream match {
      case stream if stream.isEmpty => throw new RuntimeException("stream is empty")
      case 'e' %:: tail             => (tail, builder += stream.headByte)
      case stream => {
        val keyResult = decodeString(stream, builder)
        val valueResult = doDecode(keyResult._1, keyResult._2)
        decode(valueResult._1, builder)
      }
    }
    decode(stream, builder)
  }

  def decodeString(stream: BStream, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = {
    @tailrec
    def decode(stream: BStream, length: Int, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = stream match {
      case stream if length == 0    => (stream, builder)
      case stream if stream.isEmpty => throw new RuntimeException("unexpected end of stream")
      case c %:: tail               => decode(tail, length - 1, builder += stream.headByte)
    }
    val length = getLength(stream, StringBuilder.newBuilder, builder)
    decode(length._1, length._2, length._3)
  }

  @tailrec
  private def getLength(stream: BStream, builder: StringBuilder, bbuilder: BytesBuilder): Tuple3[BStream, Int, BytesBuilder] = stream match {
    case stream if stream.isEmpty => throw new RuntimeException("unexpected end of stream: " + builder.toString)
    case c %:: tail if c.isDigit  => getLength(tail, builder.append(c), bbuilder += stream.headByte)
    case ':' %:: tail             => (tail, builder.toInt, bbuilder += stream.headByte)
  }

  private def doDecode(stream: BStream, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = stream match {
    case 'i' %:: _            => decodeInteger(stream, builder)
    case 'l' %:: tail         => decodeList(tail, builder += stream.headByte)
    case 'd' %:: tail         => decodeDictionary(tail, builder += stream.headByte)
    case c %:: _ if c.isDigit => decodeString(stream, builder)
    case _                    => throw new RuntimeException("unknown type")
  }

  private def decodeInteger(stream: BStream, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = {
    @tailrec
    def decode(stream: BStream, builder: StringBuilder, bbuilder: BytesBuilder): Tuple2[BStream, BytesBuilder] = stream match {
      case stream if stream.isEmpty            => throw new RuntimeException("unexpected enf of stream: " + builder.toString)
      case c %:: tail if c.isDigit || c == '-' => decode(tail, builder.append(c), bbuilder += stream.headByte)
      case 'i' %:: tail                        => decode(tail, builder, bbuilder += stream.headByte)
      case 'e' %:: tail                        => (tail, bbuilder += stream.headByte)
      case _                                   => throw new RuntimeException("not matched")
    }
    return decode(stream, StringBuilder.newBuilder, builder)
  }
  private def decodeList(stream: BStream, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = {
    @tailrec
    def decode(stream: BStream, builder: BytesBuilder): Tuple2[BStream, BytesBuilder] = stream match {
      case 'e' %:: tail => (tail, builder += stream.headByte)
      case stream => {
        val result = doDecode(stream, builder)
        decode(result._1, result._2)
      }
    }
    decode(stream, builder)
  }
}