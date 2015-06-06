package com.spooky

import java.util.concurrent.ConcurrentHashMap
import scala.util.Random

class RoutingTable {
  private val t = Array.fill[Bucket](160)(new Bucket)
     
}

class Bucket(val capacity:Int = 8) {
  def entries: Int = 0
  
  
  
	private val r = Random.nextInt();
  override def toString:String = {
    r + ""
  }
}