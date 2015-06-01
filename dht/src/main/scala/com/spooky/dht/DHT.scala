package com.spooky.dht

import java.math.BigInteger
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.metainfo.Torrent

object DHT {
  //bootstrap nodes
  def apply(id: NodeId, t: Torrent): DHT = new DHT(id)
}

class DHT(id: NodeId) {

  def request(search: InfoHash)(count: Int): List[Node] = {
    Nil
  }

  private def distance(o: InfoHash, c: InfoHash): Int = {
    //In Kademlia, the distance metric is XOR and the result is interpreted as an unsigned integer. distance(A,B) = |A xor B| Smaller values are closer.
    0
  }
}
