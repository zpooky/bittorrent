package com.spooky.dht

import com.spooky.bittorrent.InfoHash

abstract class Request(nodeId: NodeId)
abstract class Response(nodeId: NodeId)

//arguments:  {"id" : "<querying nodes id>"}
case class PingRequest(nodeId: NodeId) extends Request(nodeId)
//response: {"id" : "<queried nodes id>"}
case class PingResponse(nodeId: NodeId) extends Response(nodeId)

//{"id" : "<querying nodes id>", "target" : "<id of target node>"}
case class FindNodeRequest(nodeId: NodeId, target: String) extends Request(nodeId)
//{"id" : "<queried nodes id>", "nodes" : "<compact node info>"}
case class FindNodeResponse(nodeId: NodeId, nodes: NodeInfo) extends Response(nodeId)

//Compact node info = 26-byte string: NodeId(20)|ip(4)|port(2)
case class NodeInfo(nodeId: NodeId, address: Address)

//{"id" : "<querying nodes id>", "info_hash" : "<20-byte infohash of target torrent>"}
case class GetPeersRequest(nodeId: NodeId, infoHash: InfoHash) extends Request(nodeId)
//{"id" : "<queried nodes id>", "token" :"<opaque write token>", "values" : ["<peer 1 info string>", "<peer 2 info string>"]}
//or: {"id" : "<queried nodes id>", "token" :"<opaque write token>", "nodes" : "<compact node info>"}
case class GetPeersResponse(nodeId: NodeId, token: Token, nodes: List[NodeInfo]) extends Response(nodeId)

//{"id" : "<querying nodes id>",
//  "implied_port": <0 or 1>,
//  "info_hash" : "<20-byte infohash of target torrent>",
//  "port" : <port number>,
//  "token" : "<opaque token>"}
case class AnnouncePeersRequest(nodeId: NodeId, impliedPort: Boolean, infoHash: InfoHash, port: Short, token: Token) extends Request(nodeId)
//response: {"id" : "<queried nodes id>"}
case class AnnoucePeersResponse(nodeId: NodeId) extends Response(nodeId)
