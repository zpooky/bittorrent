package com.spooky.inbound.step

import com.spooky.inbound.InStep
import spooky.util.ByteString
import com.spooky.inbound.Reply
import com.spooky.inbound.OutStep
import com.spooky.cipher.MSEKeyPair

class DoneStep(val keyPair: MSEKeyPair, val data: Option[ByteString]) extends InStep with OutStep {
  def step(in: ByteString): OutStep = {
    ???
  }

  def step(reply: Reply): InStep = {
    ???
  }
}
