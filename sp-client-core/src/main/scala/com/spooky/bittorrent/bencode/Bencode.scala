package com.spooky.bittorrent.bencode

import scala.annotation.tailrec
import com.spooky.bittorrent.bencode.wtf._

abstract class BValue
case class BString(value: String) extends BValue
case class BInteger(value: Long) extends BValue
//TODO fix list to not reverse applies also to checksum list
case class BList(value: List[BValue]) extends BValue
case class BDictionary(value: List[Tuple2[BString, BValue]]) extends BValue {
  def get(search: String):Option[BValue] = {
    value.find{case (key,_) => key.value.equals(search)}.map(_._2)   
  }
}
case class BChecksum(value: Array[Byte]) extends BValue

object Bencode {

  def decode(stream: String): BValue = {
    doDecode(new StringBStream(stream))._2
  }
  def decode(stream: BStream): BValue = {
    doDecode(stream)._2
  }

  private def doDecode(stream: BStream): Tuple2[BStream, BValue] = stream match {
    case 'i' %:: _            ⇒ decodeInteger(stream)
    case 'l' %:: tail         ⇒ decodeList(tail)
    case 'd' %:: tail         ⇒ decodeDictionary(tail)
    case c %:: _ if c.isDigit ⇒ decodeString(stream)
    case _                    ⇒ null
  }

  private def decodeInteger(stream: BStream): Tuple2[BStream, BInteger] = {
    @tailrec
    def decode(stream: BStream, builder: StringBuilder): Tuple2[BStream, BInteger] = stream match {
      case stream if stream.isEmpty ⇒ throw new RuntimeException("stream is empty: " + builder.toString)
      case c %:: tail if c.isDigit  ⇒ decode(tail, builder.append(c))
      case 'i' %:: tail             ⇒ decode(tail, builder)
      case 'e' %:: tail             ⇒ (tail, BInteger(builder.toLong))
      case _                        ⇒ throw new RuntimeException("not matched")
    }
    return decode(stream, StringBuilder.newBuilder)
  }
  private def decodeList(stream: BStream): Tuple2[BStream, BList] = {
    @tailrec
    def decode(stream: BStream, list: List[BValue]): Tuple2[BStream, BList] = stream match {
      case 'e' %:: tail ⇒ (tail, BList(list))
      case stream ⇒ {
        val result = doDecode(stream)
        decode(result._1, result._2 :: list)
      }
    }
    decode(stream, List())
  }
  private def decodeDictionary(stream: BStream): Tuple2[BStream, BDictionary] = {
    @tailrec
    def decode(stream: BStream, list: List[Tuple2[BString, BValue]]): Tuple2[BStream, BDictionary] = stream match {
      case stream if stream.isEmpty ⇒ throw new RuntimeException("stream is empty")
      case 'e' %:: tail             ⇒ (tail, BDictionary(list))
      case stream ⇒ {
        val keyResult = decodeString(stream)
        val valueResult = if (keyResult._2.value.equals("pieces")) checksumList(keyResult._1) else doDecode(keyResult._1)
        decode(valueResult._1, (keyResult._2, valueResult._2) :: list)
      }
    }
    decode(stream, List())
  }

  private def decodeString(stream: BStream): Tuple2[BStream, BString] = {
    @tailrec
    def decode(stream: BStream, length: Int, builder: StringBuilder): Tuple2[BStream, BString] = stream match {
      case strean if length == 0    ⇒ (stream, BString(builder.toString))
      case stream if stream.isEmpty ⇒ throw new RuntimeException("stream is empty: " + builder.toString)
      case c %:: tail               ⇒ decode(tail, length - 1, builder.append(c))
    }
    val length = getLength(stream, StringBuilder.newBuilder)
    decode(length._1, length._2, StringBuilder.newBuilder)
  }

  private def checksumList(stream: BStream): Tuple2[BStream, BList] = {
    val length = getLength(stream, StringBuilder.newBuilder)
    @tailrec
    def chunkx(stream: BStream, index: Int,checksum:Array[Byte]): Tuple2[BStream, BChecksum] = index match {
      case 20 => (stream,BChecksum(checksum))
      case length => {
        checksum(index) = stream.headByte
        chunkx(stream.tail,length+1,checksum)
      }
    }
    @tailrec
    def decode(stream: BStream, length: Int,list:List[BChecksum]): Tuple2[BStream, List[BChecksum]] = length match {
      case 0 => (stream,list)
      case length => {
        val chunk = chunkx(stream, 0, Array.ofDim[Byte](20))
        decode(chunk._1,length-20,chunk._2 :: list)
      } 
    }
    val decoded = decode(length._1, length._2, Nil)
    (decoded._1,BList(decoded._2))
  }
  @tailrec
  private def getLength(stream: BStream, builder: StringBuilder): Tuple2[BStream, Int] = stream match {
    case stream if stream.isEmpty ⇒ throw new RuntimeException("stream is empty: " + builder.toString)
    case c %:: tail if c.isDigit  ⇒ getLength(tail, builder.append(c))
    case ':' %:: tail             ⇒ (tail, builder.toInt)
  }
}