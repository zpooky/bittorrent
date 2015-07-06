package com.spooky.inbound

import akka.util.ByteString

trait Reply {
  def reply(r: ByteString): Unit
}
