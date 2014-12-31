package com.spooky.bittorrent.bencode

import scala.annotation.tailrec
import com.spooky.bittorrent.wtf._

abstract class BValue
case class BString(value: String) extends BValue
case class BInteger(value: Long) extends BValue
case class BList(value: List[BValue]) extends BValue
case class BDictionary(value: List[Tuple2[BString, BValue]]) extends BValue

object Bencode {
	def main(args: Array[String]) {
		println(Bencode.decode("i123e"))
		println(Bencode.decode("3:was"))
		println(Bencode.decode("1:w"))
		println(Bencode.decode("4:wasd"))
		println(Bencode.decode("d1:w2:wa3:was4:wasde"))
		println(Bencode.decode("l1:w2:wa3:was4:wasde"))
		println("--")
		println(Bencode.decode("ll1:w2:wa3:was4:wasdel1:w2:wa3:was4:wasdee")) //not working
		println(Bencode.decode("d1:wl1:w2:wa3:was4:wasde2:wal1:w2:wa3:was4:wasdee"))
	}

	def decode(stream: String): BValue = {
		doDecode(stream)._2
	}

	private def doDecode(stream: String): Tuple2[String, BValue] = stream match {
		case 'i' %:: _ => decodeInteger(stream)
		case 'l' %:: tail => decodeList(tail)
		case 'd' %:: tail => decodeDictionary(tail)
		case c %:: _ if c.isDigit => decodeString(stream)
		case _ => null
	}

	private def decodeInteger(stream: String): Tuple2[String, BInteger] = {
		@tailrec
		def decode(stream: String, builder: StringBuilder): Tuple2[String, BInteger] = stream match {
			case stream if stream.isEmpty => throw new RuntimeException("stream is empty: " + builder.toString)
			case c %:: tail if c.isDigit => decode(tail, builder.append(c))
			case 'i' %:: tail => decode(tail, builder)
			case 'e' %:: tail => (tail, BInteger(builder.toLong))
			case _ => throw new RuntimeException("not matched")
		}
		return decode(stream, StringBuilder.newBuilder)
	}
	private def decodeList(stream: String): Tuple2[String, BList] = {
		@tailrec
		def decode(stream: String, list: List[BValue]): Tuple2[String, BList] = stream match {
			case 'e' %:: tail => (tail, BList(list))
			case stream => {
				val result = doDecode(stream)
				decode(result._1, result._2 :: list)
			}
		}
		decode(stream, List())
	}
	private def decodeDictionary(stream: String): Tuple2[String, BDictionary] = {
		@tailrec
		def decode(stream: String, list: List[Tuple2[BString, BValue]]): Tuple2[String, BDictionary] = stream match {
			case stream if stream.isEmpty => throw new RuntimeException("stream is empty")
			case 'e' %:: tail => (tail, BDictionary(list))
			case stream => {
				val keyResult = decodeString(stream)
				val valueResult = doDecode(keyResult._1)
				decode(valueResult._1, (keyResult._2, valueResult._2) :: list)
			}
		}
		decode(stream, List())
	}

	private def decodeString(stream: String): Tuple2[String, BString] = {
		@tailrec
		def getLength(stream: String, builder: StringBuilder): Tuple2[String, Int] = stream match {
			case stream if stream.isEmpty => throw new RuntimeException("stream is empty: " + builder.toString)
			case c %:: tail if c.isDigit => getLength(tail, builder.append(c))
			case ':' %:: tail => (tail, builder.toInt)
		}
		@tailrec
		def decode(stream: String, length: Int, builder: StringBuilder): Tuple2[String, BString] = stream match {
			case strean if length == 0 => (stream, BString(builder.toString))
			case stream if stream.isEmpty => throw new RuntimeException("stream is empty: " + builder.toString)
			case c %:: tail => decode(tail, length - 1, builder.append(c))
		}
		val length = getLength(stream, StringBuilder.newBuilder)
		decode(length._1, length._2, StringBuilder.newBuilder)
	}
}