package com.spooky.inbound

import spooky.util.ByteString

trait InStep {
  def step(in: ByteString): OutStep
}

trait OutStep {
  def step(reply: Reply): InStep
}

//object SuccessStep extends Step {
//  def step(reply: Reply)(in: ByteString): Step = SuccessStep
//}
