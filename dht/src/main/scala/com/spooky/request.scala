package com.spooky

import com.spooky.bittorrent.InfoHash
import java.net.InetSocketAddress
import akka.util.ByteString
import com.spooky.dht.Token
import com.spooky.dht.Address
import com.spooky.bencode.Bencode
import com.spooky.bencode.BDictionary
import com.spooky.bencode.BString
import scala.util.Either
import scala.util.Right
import com.spooky.bencode.BDictionary
import com.spooky.bencode.BDictionary
import com.spooky.bencode.BDictionary

sealed case class Message(data: ByteString, sender: InetSocketAddress)

object DHT {
  def parse(data: ByteString): Either[String, Exchange] = {
    Bencode.decode(data) match {
      case d: BDictionary => {
        d.get("t") match {
          case Some(BString(transactionId)) => d.get("y") match {
            case Some(BString("q")) => parseQuery(transactionId, d)
            case Some(BString("r")) => parseResponse(transactionId, d)
            case Some(BString("e")) => Right(ErrorResponse.parse(transactionId, d))
            case Some(_)            => Left("Exchange type was not a String")
            case None               => Left("Missing exchange type")
          }
          case _ => Left("No transaction id")
        }
      }
      case _ => Left("DHt message root element was not an dictionary for some reason")
    }
  }

  private def parseQuery(transactionId: String, request: BDictionary): Either[String, Exchange] = request.get("q") match {
    case Some(BString("ping"))          => Right(PingQuery.parse(transactionId, request))
    case Some(BString("find_node"))     => Right(FindNodeQuery.parse(transactionId, request))
    case Some(BString("get_peers"))     => Right(GetPeersQuery.parse(transactionId, request))
    case Some(BString("announce_peer")) => Right(AnnouncePeersQuery.parse(transactionId, request))

    case Some(BString(query))           => Left(s"Unknown method ${query}")
    case Some(_)                        => Left("Corrupt message")
    case None                           => Left("Missing query type")
  }
  private def parseResponse(transactionId: String, request: BDictionary): Either[String, Exchange] = request.get("r") match {
    case Some(d: BDictionary) if d.get("nodes").isDefined => Right(FindNodeResponse.parse(transactionId, request))
    case Some(d: BDictionary) if d.get("token").isDefined => Right(GetPeersResponse.parse(transactionId, request))
    case Some(d: BDictionary) if d.get("id").isDefined => Right(PingResponse.parse(transactionId, request))
    case Some(d: BDictionary) if d.get("id").isDefined => Right(AnnoucePeersResponse.parse(transactionId, request)) //TODO will never be called
    case Some(_) => Left("l")
    case None => Left("k")

  }

  trait Parser {
    def parse(transactionId: String, exchange: BDictionary): Exchange
  }

  sealed abstract class Exchange(transactionId: String)

  sealed abstract class Query(nodeId: InfoHash, transactionId: String) extends Exchange(transactionId)
  sealed abstract class Response(nodeId: InfoHash, transactionId: String) extends Exchange(transactionId)

  //{"t":"aa", "y":"q", "q":"ping", "a":{"id":"abcdefghij0123456789"}}
  sealed case class PingQuery(nodeId: InfoHash, transactionId: String) extends Query(nodeId, transactionId)
  object PingQuery extends Parser {
    def parse(transactionId: String, data: BDictionary): PingQuery = {
      ???
    }
  }
  //{"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
  sealed case class PingResponse(nodeId: InfoHash, transactionId: String) extends Response(nodeId, transactionId)
  object PingResponse extends Parser {
    def parse(transactionId: String, data: BDictionary): PingResponse = {
      ???
    }
  }

  //{"t":"aa", "y":"q", "q":"find_node", "a": {"id":"abcdefghij0123456789", "target":"mnopqrstuvwxyz123456"}}
  sealed case class FindNodeQuery(nodeId: InfoHash, target: String, transactionId: String) extends Query(nodeId, transactionId)
  object FindNodeQuery extends Parser {
    def parse(transactionId: String, data: BDictionary): FindNodeQuery = {
      ???
    }
  }
  //{"t":"aa", "y":"r", "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
  sealed case class FindNodeResponse(nodeId: InfoHash, nodes: NodeInfo, transactionId: String) extends Response(nodeId, transactionId)
  object FindNodeResponse {
    def parse(transactionId: String, data: BDictionary): FindNodeResponse = {
      ???
    }
  }

  //Compact node info = 26-byte string: InfoHash(20)|ip(4)|port(2)
  sealed case class NodeInfo(nodeId: InfoHash, address: Address)

  //{"t":"aa", "y":"q", "q":"get_peers", "a": {"id":"abcdefghij0123456789", "info_hash":"mnopqrstuvwxyz123456"}}
  sealed case class GetPeersQuery(nodeId: InfoHash, infoHash: InfoHash, transactionId: String) extends Query(nodeId, transactionId)
  object GetPeersQuery extends Parser {
    def parse(transactionId: String, data: BDictionary): GetPeersQuery = {
      ???
    }
  }
  //{"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "values": ["axje.u", "idhtnm"]}}
  //or: {"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "nodes": "def456..."}}
  sealed case class GetPeersResponse(nodeId: InfoHash, token: Token, nodes: List[NodeInfo], transactionId: String) extends Response(nodeId, transactionId)
  object GetPeersResponse extends Parser {
    def parse(transactionId: String, data: BDictionary): GetPeersResponse = {
      ???
    }
  }

  //{"t":"aa", "y":"q", "q":"announce_peer", "a": {"id":"abcdefghij0123456789", "implied_port": 1, "info_hash":"mnopqrstuvwxyz123456", "port": 6881, "token": "aoeusnth"}}
  sealed case class AnnouncePeersQuery(nodeId: InfoHash, impliedPort: Boolean, infoHash: InfoHash, port: Short, token: Token, transactionId: String) extends Query(nodeId, transactionId)
  object AnnouncePeersQuery extends Parser {
    def parse(transactionId: String, data: BDictionary): AnnouncePeersQuery = {
      ???
    }
  }
  //response: {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
  sealed case class AnnoucePeersResponse(nodeId: InfoHash, transactionId: String) extends Response(nodeId, transactionId)
  object AnnoucePeersResponse extends Parser {
    def parse(transactionId: String, data: BDictionary): AnnoucePeersResponse = {
      ???
    }
  }
  //{"t":"aa", "y":"e", "e":[201, "A Generic Error Ocurred"]}
  sealed abstract class ErrorCode {
    def code: Int
  }
  object ErrorCode {
    def parse(code: Int): Option[ErrorCode] = {
      ???
    }
  }
  object GenericError extends ErrorCode {
    def code = 201
  }
  object ServerError extends ErrorCode {
    def code = 202
  }
  object ProtocolError extends ErrorCode {
    def code = 203
  }
  object MethodUnknown extends ErrorCode {
    def code = 204
  }
  sealed case class ErrorResponse(transactionId: String, code: ErrorCode, message: String)
  object ErrorResponse extends Parser {
    def parse(transactionId: String, data: BDictionary): PingQuery = {
      ???
    }
  }
}