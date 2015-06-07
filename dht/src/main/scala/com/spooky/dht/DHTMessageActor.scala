package com.spooky.dht

import akka.actor.Actor
import akka.actor.ActorRef
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

class DHTMessageActor(connection: ActorRef) extends Actor {
  def receive: Receive = {
    case Message(data, sender) => {
      val parsed = DHT.parse(data)
      if(parsed.isRight){
      handle(parsed.right.get, sender)
      } else ???
    }
  }
  def handle(exchange: DHT.Exchange, sender: InetSocketAddress): Unit = exchange match {
    case r: PingQuery =>
    case r: PingResponse =>
    case r: FindNodeQuery =>
    case r: FindNodeResponse =>
    case r: GetPeersQuery =>
    case r: GetPeersResponse =>
    case r: AnnouncePeersQuery =>
    case r: AnnoucePeersResponse =>
  }
}
