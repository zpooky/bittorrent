package com.spooky.bencode

import java.nio.ByteBuffer
import scala.annotation.tailrec

class BencodeMarshall {
  def marshall(o: Any): ByteBuffer = {
    
    marshall(o, Nil)
    ???
  }
  //  @tailrec
  private def marshall(o: Any, tail: List[BValue]): List[BValue] = o match {
    case i: Int     => BInteger(i) :: tail
    case i: Long    => BInteger(i) :: tail
    case i: Short   => BInteger(i) :: tail
    case s: String  => BString(s) :: tail
    case l: List[_] => BList(convert(l, Nil)) :: tail
    case i: AnyRef  => BDictionary(???) :: tail
    case i          => throw new RuntimeException(s"unknown type ${i}")

  }
  @tailrec
  private def convert(l: List[_], result: List[BValue]): List[BValue] = l match {
    case Nil       => result
    case (x :: xs) => convert(xs, marshall(x, result))
  }
}