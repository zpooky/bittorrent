package com.spooky.inbound.step

import com.spooky.inbound.Base
import com.spooky.inbound.InStep
import com.spooky.inbound.OutStep
import com.spooky.inbound.Reply
import akka.util.ByteString
import com.spooky.bittorrent.InfoHash
import com.spooky.inbound.LocalPublicKey
import com.spooky.inbound.RemotePublicKey
import com.spooky.inbound.SecretKey
import java.nio.ByteOrder
import java.nio.ByteBuffer
import akka.util.FakeBStrings

class SendPublicKeyStep(infoHashes: List[InfoHash], publicKey: LocalPublicKey, remotePublicKey: RemotePublicKey, secretKey: SecretKey) extends Base with OutStep {
  def step(reply: Reply): ReceiveInfoStep = {
    val padding = randomPadding()
    val rawPublicKey = publicKey.raw

    println(rawPublicKey.length)
//    assert(rawPublicKey.length == 96)

    val buffer = ByteBuffer.allocate(rawPublicKey.length + padding.length).order(ByteOrder.BIG_ENDIAN)
    buffer.put(rawPublicKey)
    buffer.put(padding)

    assert(!buffer.hasRemaining)

    reply.reply(FakeBStrings(buffer.flip().asInstanceOf[ByteBuffer]))

    new ReceiveInfoStep(infoHashes, publicKey, remotePublicKey, secretKey)
  }

}
