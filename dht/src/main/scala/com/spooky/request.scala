package com.spooky

import com.spooky.bittorrent.InfoHash
import java.net.InetSocketAddress
import akka.util.ByteString
import com.spooky.dht.Token
import com.spooky.dht.Address

sealed case class Message(data: ByteString, sender: InetSocketAddress)

object DHT {
  def parse(data: ByteString): Exchange = {
    ???
  }
  
  sealed abstract class Exchange

  sealed abstract class Request(nodeId: InfoHash) extends Exchange
  sealed abstract class Response(nodeId: InfoHash) extends Exchange

  //arguments:  {"id" : "<querying nodes id>"}
  sealed case class PingRequest(nodeId: InfoHash) extends Request(nodeId)
  //response: {"id" : "<queried nodes id>"}
  sealed case class PingResponse(nodeId: InfoHash) extends Response(nodeId)

  //{"id" : "<querying nodes id>", "target" : "<id of target node>"}
  sealed case class FindNodeRequest(nodeId: InfoHash, target: String) extends Request(nodeId)
  //{"id" : "<queried nodes id>", "nodes" : "<compact node info>"}
  sealed case class FindNodeResponse(nodeId: InfoHash, nodes: NodeInfo) extends Response(nodeId)

  //Compact node info = 26-byte string: InfoHash(20)|ip(4)|port(2)
  sealed case class NodeInfo(nodeId: InfoHash, address: Address)

  //{"id" : "<querying nodes id>", "info_hash" : "<20-byte infohash of target torrent>"}
  sealed case class GetPeersRequest(nodeId: InfoHash, infoHash: InfoHash) extends Request(nodeId)
  //{"id" : "<queried nodes id>", "token" :"<opaque write token>", "values" : ["<peer 1 info string>", "<peer 2 info string>"]}
  //or: {"id" : "<queried nodes id>", "token" :"<opaque write token>", "nodes" : "<compact node info>"}
  sealed case class GetPeersResponse(nodeId: InfoHash, token: Token, nodes: List[NodeInfo]) extends Response(nodeId)

  //{"id" : "<querying nodes id>",
  //  "implied_port": <0 or 1>,
  //  "info_hash" : "<20-byte infohash of target torrent>",
  //  "port" : <port number>,
  //  "token" : "<opaque token>"}
  sealed case class AnnouncePeersRequest(nodeId: InfoHash, impliedPort: Boolean, infoHash: InfoHash, port: Short, token: Token) extends Request(nodeId)
  //response: {"id" : "<queried nodes id>"}
  sealed case class AnnoucePeersResponse(nodeId: InfoHash) extends Response(nodeId)
}