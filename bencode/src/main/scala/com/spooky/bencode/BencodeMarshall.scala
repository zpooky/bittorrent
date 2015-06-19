package com.spooky.bencode

import java.nio.ByteBuffer
import scala.annotation.tailrec
import scala.reflect.runtime.universe._

class BencodeMarshall {
  //  def marshall[T: TypeTag](o: T): List[BValue] = marshall(o, Nil)
  //  @tailrec
  def marshall[T: TypeTag](o: T): BValue = o match {
    case i: Int     => BInteger(i)
    case i: Long    => BInteger(i)
    case i: Short   => BInteger(i)
    case s: String  => BString(s)
    case b: Boolean => BInteger(if (b) 1 else 0)
    case l: List[_] => BList(convert(l, Nil))
    case a: Array[Byte] => {
      BChecksum(a)
    }
    case i: AnyRef => {
      println(i.getClass.getSimpleName)
      val m = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
      val im = m.reflect(i)
      val tList = typeOf[T].members.collect {
        case m: MethodSymbol if m.isCaseAccessor => m
      }.map(methodSymbol => {
        val method = im.reflectMethod(methodSymbol)
        (BString(name(methodSymbol.name.toTermName)), marshall(method()))
      }).toList
      BDictionary(tList)
    }
    case null => ???
    case i    => throw new RuntimeException(s"unknown type ${i}")

  }
  @tailrec
  private def convert(l: List[_], result: List[BValue]): List[BValue] = l match {
    case Nil       => result
    case (x :: xs) => convert(xs, marshall(x) :: result)
  }

  private def name(name: TermName): String = {
    name.toString.split('$')(0)
  }
}