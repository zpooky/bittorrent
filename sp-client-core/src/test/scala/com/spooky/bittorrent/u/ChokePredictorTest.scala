package com.spooky.bittorrent.u

import org.scalatest.FunSuite
import com.spooky.bittorrent.l.session.client.ClientSession
import com.spooky.bittorrent.protocol.client.pwp.api.Request
import java.nio.ByteBuffer

class ChokePredictorTest extends FunSuite {
  test("t") {
    val session = sessionx
    val cp = new ChokePredictor(Size(100, Byte), session)
    for (n <- 1 to 9) {
      assert(!session.choking)
      cp.incomming(request)
    }
    assert(!session.choking)
    cp.incomming(request(1))
    assert(session.choking)
    cp.outgoing(AbortPlaceholder(12))
    assert(!session.choking)
    cp.incomming(request(12))
    assert(session.choking)
  }

  def request: Request = Request(0, 0, 10)
  def request(i: Int): Request = Request(0, 0, i)
  def sessionx = new ClientSession(null) {

    override def choke(): Unit = {
      choking = true
    }

    override def unchoke(): Unit = {
      choking = false
    }
  }
}
