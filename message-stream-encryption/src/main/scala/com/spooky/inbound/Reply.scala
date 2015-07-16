package com.spooky.inbound

import spooky.util.ByteString

trait Reply {
  def reply(r: ByteString): Unit
}
