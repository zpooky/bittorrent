package com.spooky.bencode

import scala.reflect.io.Streamable.Bytes

sealed abstract class BOption
sealed case class BSome[T](value: T) extends BOption
sealed case class BNone[T]() extends BOption

sealed abstract class BValue {
  def toBencode: String
  def toBDictionary: Option[BDictionary] = this match {
    case (d: BDictionary) => Some(d)
    case _                => None
  }
  def toBString: Option[BString] = this match {
    case (s: BString) => Some(s)
    case _            => None
  }
}
sealed case class BString(value: String) extends BValue {
  override def toBencode = value.length + ":" + value
}
sealed case class BInteger(value: Long) extends BValue {
  override def toBencode = "i" + value + "e"
}
sealed case class BList(value: List[BValue]) extends BValue {
  override def toBencode = value.foldLeft(StringBuilder.newBuilder.append("l"))((first, current) => first.append(current.toBencode)).toString + "e"
}
sealed case class BDictionary(value: List[Tuple2[BString, BValue]]) extends BValue {
  def get(search: String): Option[BValue] = {
    value.find { case (key, _) => key.value.equals(search) }.map(_._2)
  }
  //TODO need to sort list according to spec
  override def toBencode = value.foldLeft(StringBuilder.newBuilder.append("d"))((previous, current) => previous.append(current._1.toBencode).append(current._2.toBencode)).toString + "e"
}
case class BChecksum(value: Array[Byte]) extends BValue {
  override def toBencode = ???
}
