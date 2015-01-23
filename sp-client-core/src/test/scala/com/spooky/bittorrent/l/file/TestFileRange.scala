package com.spooky.bittorrent.l.file

import org.scalatest.FunSuite
import com.spooky.bittorrent.Binary

class TestFileRange extends FunSuite {
  test("test") {
    val range = FileRange(1000).add(2, 6).add(1, 5).add(200, 500).add(6, 250).add(501, 530).add(0, 2)
    println(range)
    val size = 100
    val have = range.have(size) _
    for (index <- 0 to 1000 / size) {
      val start = index * size
      println(start + "-" + (start + size) + "|" + have(index))
    }
    println(Binary.toBinary(range.bitset(size)))
  }
  test("2") {
    val range = FileRange(1000).add(0, 100).add(100,250).add(400, 500)
    val size = 100
    println(range)
    val have = range.have(size) _
    assert(have(0))
    assert(have(1))
    assert(have(2) == false)
    assert(have(3) == false)
    assert(have(4))
  }
}

