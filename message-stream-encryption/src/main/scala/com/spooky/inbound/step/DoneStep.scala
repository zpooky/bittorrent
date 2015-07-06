package com.spooky.inbound.step

import com.spooky.inbound.InStep
import akka.util.ByteString
import com.spooky.inbound.Reply
import com.spooky.inbound.OutStep
import com.spooky.cipher.WriteCipher
import com.spooky.cipher.ReadCipher

class DoneStep(writeCipher: WriteCipher, readCipher: ReadCipher) extends InStep with OutStep {
  def step(in: ByteString, reply: Reply): OutStep = {
    ???
  }

  def step(reply: Reply): InStep = {
    ???
  }
}
