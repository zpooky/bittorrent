package com.spooky

import com.spooky.bittorrent.mse.PublicKey

trait Party {

  def receivePublic(publicKey: PublicKey): Unit
  def sendPublic: PublicKey

  def send: Array[Byte]
  def receive(chiperText: Array[Byte])
}
