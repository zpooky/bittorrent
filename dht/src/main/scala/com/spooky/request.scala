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
import com.spooky.bencode.BString
import com.spooky.bencode.BString
import com.spooky.bencode.BValue
import com.spooky.bencode.BInteger

sealed case class Message(data: ByteString, sender: InetSocketAddress)

object DHT {
  def parse(data: ByteString): Either[String, Exchange] = {
    Bencode.decode(data) match {
      case d: BDictionary => {
        d.get("t") match {
          case Some(BString(transactionId)) => d.get("y") match {
            case Some(BString("q")) => parseQuery(transactionId, d)
            case Some(BString("r")) => parseResponse(transactionId, d)
            case Some(BString("e")) => ErrorResponse.parse(transactionId)(d).map(Right(_)).getOrElse(Left("Invalid exchange"))
            case Some(_)            => Left("Exchange type was not a String")
            case None               => Left("Missing exchange type")
          }
          case _ => Left("No transaction id")
        }
      }
      case _ => Left("DHt message root element was not an dictionary for some reason")
    }
  }

  private def parseQuery(transactionId: String, request: BDictionary): Either[String, Exchange] = {
    val pf = p[Query](request) _
    request.get("q") match {
      case Some(BString("ping"))          => pf(PingQuery.parse(transactionId))
      case Some(BString("find_node"))     => pf(FindNodeQuery.parse(transactionId))
      case Some(BString("get_peers"))     => pf(GetPeersQuery.parse(transactionId))
      case Some(BString("announce_peer")) => pf(AnnouncePeersQuery.parse(transactionId))

      case Some(BString(query))           => Left(s"Unknown method ${query}")
      case Some(_)                        => Left("Corrupt message")
      case None                           => Left("Missing query type")
    }
  }
  private def parseResponse(transactionId: String, request: BDictionary): Either[String, Exchange] = {
    val pf = p[Response](request) _
    request.get("r") match {
      case Some(d: BDictionary) if d.get("nodes").isDefined => pf(FindNodeResponse.parse(transactionId))
      case Some(d: BDictionary) if d.get("token").isDefined => pf(GetPeersResponse.parse(transactionId))
      case Some(d: BDictionary) if d.get("id").isDefined => pf(PingResponse.parse(transactionId))
      case Some(d: BDictionary) if d.get("id").isDefined => pf(AnnoucePeersResponse.parse(transactionId)) //TODO will never be called
      case Some(_) => Left("l")
      case None => Left("k")
    }
  }
  private def p[E <: Exchange](request: BDictionary)(f: BDictionary => Option[E]): Either[String, E] = request.get("a") match {
    case Some(d: BDictionary) => f(d).map(Right(_)).getOrElse(Left("Invalid exchange"))
    case Some(_)              => Left("a is not a dict")
    case None                 => Left("a is missing")
  }

  trait Parser {
    def parse(transactionId: String)(data: BDictionary): Option[Exchange]
  }

  sealed abstract class Exchange(transactionId: String)

  sealed abstract class Query(transactionId: String) extends Exchange(transactionId)
  sealed abstract class Response(nodeId: InfoHash, transactionId: String) extends Exchange(transactionId)

  case class IdArgument(private[DHT] val id: Array[Byte])
  object IdArgument {
    def apply(infoHash: InfoHash): IdArgument = IdArgument(infoHash.sum)
  }

  //{"t":"aa", "y":"q", "q":"ping", "a":{"id":"abcdefghij0123456789"}}
  sealed case class PingQuery(private val t: String, private val y: String = "q", private val q: String = "ping", private val a: IdArgument) extends Query(t) {
    def transactionId: String = t
    def nodeId: InfoHash = InfoHash(a.id)
  }
  object PingQuery extends Parser {
    def apply(nodeId: InfoHash, transactionId: String): PingQuery = PingQuery(t = transactionId, a = IdArgument(nodeId))

    def parse(transactionId: String)(data: BDictionary): Option[PingQuery] = {
      println(transactionId + "|" + data)
      //      data.get("id") match {
      //        case Some(BString(s)) => Some(PingQuery(InfoHash.hex(s), transactionId))
      //        case _                => ???
      //      }
      for {
        nodeId <- data.get("id")
        BString(id) <- nodeId
      } yield PingQuery(InfoHash.hex(id), transactionId)
    }
  }
  //{"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
  sealed case class PingResponse(nodeId: InfoHash, transactionId: String) extends Response(nodeId, transactionId)
  object PingResponse extends Parser {
    def parse(transactionId: String)(data: BDictionary): Option[PingResponse] = {
      for {
        nodeId <- data.get("id")
        BString(id) <- nodeId
      } yield PingResponse(InfoHash.hex(id), transactionId)
    }
  }

  //{"t":"aa", "y":"q", "q":"find_node", "a": {"id":"abcdefghij0123456789", "target":"mnopqrstuvwxyz123456"}}
  sealed case class FindNodeQuery(nodeId: InfoHash, target: String, transactionId: String) extends Query( transactionId)
  object FindNodeQuery extends Parser {
    def parse(transactionId: String)(data: BDictionary): Option[FindNodeQuery] = {
      for {
        nodeId <- data.get("id")
        BString(id) <- nodeId
        target <- data.get("target")
        BString(t) <- target
      } yield FindNodeQuery(InfoHash.hex(id), t, transactionId)
    }
  }
  //{"t":"aa", "y":"r", "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
  sealed case class FindNodeResponse(nodeId: InfoHash, nodes: NodeInfo, transactionId: String) extends Response(nodeId, transactionId)
  object FindNodeResponse {
    def parse(transactionId: String)(data: BDictionary): Option[FindNodeResponse] = {
      for {
        nodeId <- data.get("id")
        BString(id) <- nodeId
        nodes <- data.get("nodes")
        nodeInfo <- NodeInfo.parse(nodes)
      } yield FindNodeResponse(InfoHash.hex(id), nodeInfo, transactionId)
    }
  }

  //Compact node info = 26-byte string: InfoHash(20)|ip(4)|port(2)
  sealed case class NodeInfo(nodeId: InfoHash, address: Address)
  object NodeInfo {
    def parse(v: BValue): Option[NodeInfo] = v match {
      case BString(s) => {
        None
      }
      case _ => None
    }
  }

  //{"t":"aa", "y":"q", "q":"get_peers", "a": {"id":"abcdefghij0123456789", "info_hash":"mnopqrstuvwxyz123456"}}
  sealed case class GetPeersQuery(nodeId: InfoHash, infoHash: InfoHash, transactionId: String) extends Query( transactionId)
  object GetPeersQuery extends Parser {
    def parse(transactionId: String)(data: BDictionary): Option[GetPeersQuery] = {
      for {
        nId <- data.get("id")
        BString(nodeId) <- nId
        infoHash <- data.get("info_hash")
        BString(ih) <- infoHash
      } yield GetPeersQuery(InfoHash.hex(nodeId), InfoHash.hex(ih), transactionId)
    }
  }
  //{"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "values": ["axje.u", "idhtnm"]}}
  //or: {"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "nodes": "def456..."}}
  sealed case class GetPeersResponse(nodeId: InfoHash, token: Token, nodes: List[NodeInfo], transactionId: String) extends Response(nodeId, transactionId)
  object GetPeersResponse extends Parser {
    def parse(transactionId: String)(data: BDictionary): Option[GetPeersResponse] = {
      //      for {
      //        nId <- data.get("id")
      //        BString(nodeId) <- nId
      //
      //        t <- data.get("token")
      //        BString(token) <- t
      //      } yield GetPeersResponse(InfoHash.hex(nodeId), Token(token, null), nodes, transactionId)
      ???
    }
  }

  //{"t":"aa", "y":"q", "q":"announce_peer", "a": {"id":"abcdefghij0123456789", "implied_port": 1, "info_hash":"mnopqrstuvwxyz123456", "port": 6881, "token": "aoeusnth"}}
  sealed case class AnnouncePeersQuery(nodeId: InfoHash, impliedPort: Boolean, infoHash: InfoHash, port: Short, token: Token, transactionId: String) extends Query( transactionId)
  object AnnouncePeersQuery extends Parser {
    def parse(transactionId: String)(data: BDictionary): Option[AnnouncePeersQuery] = {
      for {
        nId <- data.get("id")
        BString(nodeId) <- nId

        iP <- data.get("implied_port")
        BInteger(impliedPort) <- iP

        ih <- data.get("info_hash")
        BString(infoHash) <- ih

        p <- data.get("port")
        BInteger(port) <- p

        t <- data.get("token")
        BString(token) <- t
      } yield AnnouncePeersQuery(InfoHash.hex(nodeId), impliedPort == 1, InfoHash.hex(infoHash), port.toShort, Token(token, null), transactionId)
    }
  }
  //response: {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
  sealed case class AnnoucePeersResponse(nodeId: InfoHash, transactionId: String) extends Response(nodeId, transactionId)
  object AnnoucePeersResponse extends Parser {
    def parse(transactionId: String)(data: BDictionary): Option[AnnoucePeersResponse] = {
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
    def parse(transactionId: String)(data: BDictionary): Option[PingQuery] = {
      ???
    }
  }
}
