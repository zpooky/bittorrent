package com.spooky.bittorrent

import org.scalatest.FunSuite
import scala.collection.mutable.Queue

class QueueTest extends FunSuite {
  test("t") {
    val q = Queue[Int]()
    q += 1
    q += 2
    q += 3
    q += 4
    q += 5
    Range(0, q.length).foreach { _ =>
      q.dequeue()
    }
    println("size:" + q.size)
  }
}
