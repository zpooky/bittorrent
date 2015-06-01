package com.spooky.bittorrent

import java.security.MessageDigest

abstract class Algorithm(val bytes: Int) extends Base {
  override def toString = this.getClass.getSimpleName.replace("$", "")
  def newMessageDigest = MessageDigest.getInstance(toString)
}
