package com.spooky.bittorrent.l.file

import java.util.BitSet
import FileRange._
class FileRange(size: Long, chunks: List[Range]) {
  def bitset(chunkSize: Int): BitSet = {
    val bits = Math.ceil(size / chunkSize).asInstanceOf[Int]
    val bitset = new BitSet(bits)
    val hve = have(chunkSize) _
    for (index <- 0 until bits) {
      bitset.set(index, hve(index))
    }
    println(bits)
    bitset
  }
  def have(chunkSize: Int)(index: Int): Boolean = {
    val start = index * chunkSize
    val end = start + chunkSize
    inRange((start, end))
  }
  private def overlapStart(search: Range, other: Range): Boolean = (search._1 >= other._1 && search._1 <= other._2)
  private def overlapEnd(search: Range, other: Range): Boolean = (search._2 >= other._1 && search._2 <= other._2)
  private def inRange(search: Range): Boolean = chunks.find(c => intersect(search, c)).isDefined
  private def intersect(search: Range, other: Range): Boolean = overlapStart(search, other) && overlapEnd(search, other)
  def add(start: Long, end: Long): FileRange = {
      def overlap(search: Range, other: Range): Boolean = overlapStart(search, other) || overlapEnd(search, other)
      def min(search: List[Range]): Long = search.reduce((f, s) => if (f._1 < s._1) f else s)._1
      def max(search: List[Range]): Long = search.reduce((f, s) => if (f._2 > s._2) f else s)._2
    val in = (start, end)
    val (inRange, other) = chunks.partition(current => overlap(in, current))
    val range = in :: inRange
    val head: Range = (min(range), max(range))
    new FileRange(size, head :: other)
  }
  override def toString: String = {
    val builder = StringBuilder.newBuilder
    builder.append(size).append("|")
    chunks.foreach(r => {
      builder.append("(").append(r._1).append(",").append(r._2).append(")")
    })
    builder.toString
  }
}
object FileRange {
  type Range = Tuple2[Long, Long]
  def apply(size: Long): FileRange = new FileRange(size, Nil)
}
