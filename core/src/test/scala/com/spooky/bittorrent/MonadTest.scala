package com.spooky.bittorrent

import org.scalatest.FunSuite

class MonadTest extends FunSuite {
  implicit def toOption[T](any: T): Option[T] = Option(any)
  var c = 0
  def func: Option[String] = c match {
    case 0 => {
      c = c + 1
      "0"
    }
    case 1 => {
      c = c + 1
      "1"
    }
    case 2 => {
      c = c + 1
      "2"
    }
    case _ => None
  }
  test("") {
    val xsss = for {
      x <- func
    } yield x
    println(xsss)
  }

}
