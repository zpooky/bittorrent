package com.spooky

import org.scalatest.FunSuite
import com.spooky.DHT.PingQuery
import com.spooky.bittorrent.InfoHash
import com.spooky.bencode.BencodeMarshall

class RequestTest extends FunSuite {
    //{"t":"aa", "y":"q", "q":"ping", "a":{"id":"abcdefghij0123456789"}}
  val m = new BencodeMarshall
  test("PingQuery") {
    val pingQuery = PingQuery(InfoHash.hex("abcdef0123456789"),"aa")
    val pq = m.marshall(pingQuery)
    println(pq)
    println(pq.toBencode)
    println(pingQuery.nodeId)
  }
  //{"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
  test("PingResponse") {
	  
  }
  test("FindNodeQuery") {
	  
  }
  test("FindNodeResponse") {
	  
  }
  test("") {
	  
  }
  test("GetPeersQuery") {
	  
  }
  test("GetPeersResponse") {
	  
  }
  test("AnnouncePeersQuery") {
	  
  }
  test("AnnoucePeersResponse") {
	  
  }
  test("ErrorResponse") {
	  
  }
}