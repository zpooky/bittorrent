package com.spooky.dht

import spooky.actor.Actor
import spooky.actor.ActorRef
import com.spooky.Message
import com.spooky.DHT
import java.net.InetSocketAddress
import com.spooky.DHT.PingQuery
import com.spooky.DHT.PingResponse
import com.spooky.DHT.FindNodeQuery
import com.spooky.DHT.FindNodeResponse
import com.spooky.DHT.GetPeersQuery
import com.spooky.DHT.GetPeersResponse
import com.spooky.DHT.AnnouncePeersQuery
import com.spooky.DHT.AnnoucePeersResponse
import com.spooky.bencode.ByteStringBStream
import spooky.io.Udp
import spooky.util.ByteString

class DHTMessageActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case Message(data, address, sender) => {
      if (data.isEmpty) {
        println(s"data was empty from ${address.getAddress}")
        sender ! Udp.Send(ByteString("error"), address)
      } else {
        println(data.length + "|" + new ByteStringBStream(data, 0))
        val parsed = DHT.parse(data)
        if (parsed.isRight) {
          handle(parsed.right.get, address)
        } else println(parsed.left)
      }
    }
  }
  def handle(exchange: DHT.Exchange, sender: InetSocketAddress): Unit = exchange match {
    case r => println(r)
    //    case r: PingQuery =>
    //    case r: PingResponse =>
    //    case r: FindNodeQuery =>
    //    case r: FindNodeResponse =>
    //    case r: GetPeersQuery =>
    //    case r: GetPeersResponse =>
    //    case r: AnnouncePeersQuery =>
    //    case r: AnnoucePeersResponse =>
  }
}
