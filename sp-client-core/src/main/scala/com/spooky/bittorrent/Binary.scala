package com.spooky.bittorrent

import java.util.BitSet

object Binary {
  def toBinary(b1: Byte): String = {
    String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
  }
  def toBinary(i: Int): String = {
    String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
  }
  def toBinary(l: java.lang.Long): String = {
    String.format("%64s", java.lang.Long.toBinaryString(l)).replace(' ', '0');
  }
  def toBinary(s: BitSet): String = {
    var buffer = StringBuilder.newBuilder
    for (chunk <- s.toByteArray) {
      buffer = buffer.append(toBinary(chunk))
    }
    buffer.toString
  }

}
