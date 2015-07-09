package com.spooky.bittorrent.u

import org.scalatest.FunSuite
import com.spooky.bittorrent.l.session.client.ClientSession
import com.spooky.bittorrent.protocol.client.pwp.api.Request
import java.nio.ByteBuffer
import com.spooky.bittorrent.Showable

class ChokePredictorTest extends FunSuite {
  private implicit def write: Showable => Unit = null
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
  def sessionx = new ClientSession(null, null) {

    override def choke()(implicit write: Showable => Unit): Unit = {
      choking = true
    }

    override def unchoke()(implicit write: Showable => Unit): Unit = {
      choking = false
    }
  }
}
