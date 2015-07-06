package com.spooky.inbound.step

import com.spooky.inbound.OutStep
import com.spooky.inbound.Reply
import com.spooky.inbound.InStep
import akka.util.ByteString
import com.spooky.cipher.ReadCipher
import com.spooky.cipher.WriteCipher
import com.spooky.cipher.WriteCipher

class WriteStep(writeCipher: WriteCipher) extends OutStep {
  private[step] var readStep: ReadStep = null
  def step(reply: Reply): InStep = {
    readStep
  }
}

object ReadStep {
  def apply(writeCipher: WriteCipher, readCipher: ReadCipher): ReadStep = {
    val ws = new WriteStep(writeCipher)
    val rs = new ReadStep(readCipher, ws)
    ws.readStep = rs
    rs
  }
}
class ReadStep(readCipher: ReadCipher, writeStep: WriteStep) extends InStep {

  def step(in: ByteString, reply: Reply): OutStep = {

    writeStep
  }
}
