package com.spooky.dht

import java.util.concurrent.ThreadLocalRandom
import java.time.LocalDateTime

object NodeId {
  def random: NodeId = {
    val random = ThreadLocalRandom.current
    val raw = new Array[Byte](20)
    random.nextBytes(raw)
    NodeId(raw) //TODO hash?
  }
}
case class NodeId(raw: Array[Byte])
case class Node(nodeId: NodeId, address: Address, token: Token, stateInfo: StateInfo)
//Compact IP-address/port info 6 bytes = ip(4)|port(2)
case class Address(ip: String, port: Short)
sealed abstract class NodeState
object Good extends NodeState
object Bad extends NodeState
case class StateInfo(state: NodeState, refreshTry: LocalDateTime, missedPings: Int = 0)

object Token {
  //SHA1 hash of the IP address concatenated onto a secret that changes every five minutes and tokens up to ten minutes old are accepted.

}
case class Token(s: String, d: LocalDateTime)

//PLACEHOLDEr
//case class InfoHash(s: Array[Byte])
//abstract class Torrent