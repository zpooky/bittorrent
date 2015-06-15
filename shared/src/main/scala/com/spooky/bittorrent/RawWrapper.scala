package com.spooky.bittorrent

import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset

abstract class RawWrapper(val raw: Array[Byte]) {
  override def toString = Hex.encodeHexString(raw)
  def toInteger: java.math.BigInteger = new java.math.BigInteger(raw)
}
