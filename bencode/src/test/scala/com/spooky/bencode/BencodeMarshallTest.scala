package com.spooky.bencode

import org.scalatest.FunSuite

class BencodeMarshallTest extends FunSuite {
  case class Tttt(s: String, i: Int)
  val m = new BencodeMarshall
  test("dd"){
    val t = Tttt("ss",112)
    println(m.marshall(t))
    
  }
  test("null"){
//    println(m.marshall(null))
  }
}