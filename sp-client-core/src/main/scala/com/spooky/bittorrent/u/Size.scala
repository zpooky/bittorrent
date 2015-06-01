package com.spooky.bittorrent.u

import scala.annotation.tailrec

sealed abstract class Scale extends Ordered[Scale] {
  def apply(capacity: Double): Size = Size(capacity, this)
  def bytes: Int
  def convert(size: Size): Size = size match {
    case o if o.scale == this => o
    case o => {
      val v = o.scale.bytes.asInstanceOf[Double] / bytes.asInstanceOf[Double]
      Size(v * size.capacity, this)
    }
  }
  override def compare(o: Scale): Int = bytes.compareTo(o.bytes)
  override def toString: String = getClass.getSimpleName.replace("$", "")
}
sealed case class Size(capacity: Double, scale: Scale) extends Ordered[Size] {
  def toBytes: Size = Byte.convert(this)
  def toKiloByte: Size = KiloByte.convert(this)
  def toMegaByte: Size = MegaByte.convert(this)
  def toGigaByte: Size = GigaByte.convert(this)

  def percentageFor(other: Size): Double = {
    val i = toBytes.capacity / 100
    other.toBytes.capacity / i
  }
  @tailrec
  override def compare(o: Size): Int = scale.compare(o.scale) match {
    case 0 => capacity.compare(o.capacity)
    case 1 => o.scale.convert(this).compare(o)
    case -1 => compare(scale.convert(o))
  }
  override def equals(o: Any): Boolean = o match {
    case e: Size => e.capacity == capacity && e.scale == scale
    case _       => false
  }

}
object Byte extends Scale {
  override val bytes = 1
}
object KiloByte extends Scale {
  override val bytes = 1024 * Byte.bytes
}
object MegaByte extends Scale {
  override val bytes = 1024 * KiloByte.bytes
}
object GigaByte extends Scale {
  override val bytes = 1024 * MegaByte.bytes
}
