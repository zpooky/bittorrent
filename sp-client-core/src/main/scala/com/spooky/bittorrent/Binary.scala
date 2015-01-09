package com.spooky.bittorrent

object Binary {
  def toBinary(b1: Byte) = {
    String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
  }
  def toBinary(i: Int) = {
    String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
  }
  def toBinary(l: java.lang.Long) = {
    String.format("%64s", java.lang.Long.toBinaryString(l)).replace(' ', '0');
  }
}
