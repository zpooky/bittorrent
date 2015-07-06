package com.spooky.inbound

import akka.util.ByteString

trait InStep {
  def step(in: ByteString, reply: Reply): OutStep
}

trait OutStep {
  def step(reply: Reply): InStep
}

//object SuccessStep extends Step {
//  def step(reply: Reply)(in: ByteString): Step = SuccessStep
//}
