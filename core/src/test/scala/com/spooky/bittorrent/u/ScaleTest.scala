package com.spooky.bittorrent.u

import org.scalatest.FunSuite

class ScaleTest extends FunSuite {
  test("convert up") {
    val oneGBasByte = Size(1073741824l, Byte)
    assert(oneGBasByte.toBytes == oneGBasByte)
    assert(oneGBasByte.toKiloByte == Size(1048576, KiloByte))
    assert(oneGBasByte.toMegaByte == Size(1024, MegaByte))
    assert(oneGBasByte.toGigaByte == Size(1, GigaByte))
  }

  test("convert down") {
    val oneGB = GigaByte(1)
    assert(oneGB.toGigaByte == GigaByte(1))
    assert(oneGB.toMegaByte == MegaByte(1024))
    assert(oneGB.toKiloByte == KiloByte(1024 * 1024))
    assert(oneGB.toBytes == Byte(1024 * 1024 * 1024))
  }

  test("eq") {
    val oneGB = GigaByte(1)
    assert(oneGB.compare(MegaByte(1024)) == 0)
    assert(oneGB.compare(KiloByte(1024 * 1024)) == 0)
    assert(oneGB.compare(Byte(1024 * 1024 * 1024)) == 0)
  }
  test("gt") {
    val oneGB = GigaByte(1)
    assert(oneGB.compare(MegaByte(1024 + 1)) < 0)
    assert(oneGB.compare(KiloByte(1024 * 1024 + 1)) < 0)
    assert(oneGB.compare(Byte(1024 * 1024 * 1024 + 1)) < 0)
  }
  test("lt") {
    val oneGB = GigaByte(1)
    assert(oneGB.compare(MegaByte(1024 - 1)) > 0)
    assert(oneGB.compare(KiloByte(1024 * 1024 - 1)) > 0)
    assert(oneGB.compare(Byte(1024 * 1024 * 1024 - 1)) > 0)
  }

  test("%"){
    assert(KiloByte(100).percentageFor(KiloByte(91)) == 91)
    assert(Byte(100).percentageFor(Byte(91)) == 91)
  }
}
