package com.spooky.bittorrent

import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset
import com.spooky.bencode.ByteBStream
import com.spooky.bencode.BStream
import com.spooky.bencode.wtf._
import com.spooky.bencode.EmptyBSream
import com.spooky.bencode.StringBStream

object FormatBencode {
  class Tab {
    var tabs = 0
    def inc = tabs = tabs + 1
    def dec = tabs = tabs - 1
    def p[T](a: T): T = {
      for (_ <- 0 to tabs) {
        print("  ")
      }
      print(a)
      a
    }
  }
  val UTF8 = Charset.forName("UTF8")
  def main(args: Array[String]): Unit = {
    val hex = "64313a6164323a696432303ab967c74a9a65948fd56339eae3ac95bed9e0e65165313a71343a70696e67313a74323a00cc313a76343a4c54000f313a79313a7165"
    val decoded = new ByteBStream(Hex.decodeHex(hex.toCharArray()), 0)
//    val decoded = new StringBStream("d1:rd2:id20:01234567890123456789e2:ip4:11111:t8:123456781:v4:LT011:y1:re")
    format(decoded)(new Tab)
  }
  def format(s: BStream)(implicit t: Tab): BStream = s match {
    case 'l' %:: tail => {
      t.p("[")
      decodeList(tail)
      println("]")
      EmptyBSream
    }
    case 'd' %:: tail => {
      t.inc
      println("d")
      decodeDict(tail)
    }
    case 'i' %:: tail => {
      decodeNumeric(tail)
    }
    case s @ (i %:: _) if i.isDigit => {
      val (length, tail) = getlength(s)
      decodeString(length)(tail)
    }
    case c => throw new RuntimeException(s"unknown: '$c'")
  }

  def decodeList(s: BStream)(implicit t: Tab): BStream = s match {
    case 'e' %:: tail => {
      tail
    }
    case x => decodeList(format(x))
  }

  def decodeDict(s: BStream)(implicit t: Tab): BStream = s match {
    case 'e' %:: tail => {
      t.dec
      t.p("e")
//      println("")
      tail
    }
    case x => {
      t.p("")
      val t1 = format(s)
      print(" - ")
      val t2 = format(t1)
      println
      decodeDict(t2)
    }
  }

  def decodeNumeric(tail: BStream, builder: StringBuilder = StringBuilder.newBuilder): BStream = tail match {
    case EmptyBSream => throw new RuntimeException("empty")
    case 'e' %:: tail => {
      print(builder.toString)
      tail
    }
    case c %:: tail => decodeNumeric(tail, builder.append(c))
  }

  def decodeString(length: Int)(s: BStream, b: StringBuilder = StringBuilder.newBuilder): BStream = if (length > 0)
    s match {
      case EmptyBSream          => throw new RuntimeException("empty")
      case c %:: tail => decodeString(length - 1)(tail, b.append(c))
    }
  else {
    val str = b.toString
    if(isAlfaNumeric(str)){
      print(str)
    } else print(Hex.encodeHexString(str.getBytes(UTF8)))
    s
  }

  def getlength(s: BStream, b: StringBuilder = StringBuilder.newBuilder): Tuple2[Int, BStream] = s match {
    case i %:: tail if i.isDigit => getlength(tail, b.append(i))
    case ':' %:: tail                => (b.toInt, tail)
  }

  def isAlfaNumeric(str: String):Boolean = str match {
    case "" => true
    case c %:: tail if c.isLetterOrDigit || c.isSpaceChar || c.isValidChar || c.isWhitespace => isAlfaNumeric(tail)
    case _ => false
  }

}