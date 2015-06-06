package com.spooky.dht

import akka.actor.Actor
import akka.actor.ActorRef
import com.spooky.Message
import com.spooky.DHT

class DHTMessageActor(connection: ActorRef) extends Actor {
  def receive: Receive = {
    case Message(data, sender) => {
      handle(DHT.parse(data))
    }
  }
  def handle(exchange: DHT.Exchange): Unit = {

  }
}
