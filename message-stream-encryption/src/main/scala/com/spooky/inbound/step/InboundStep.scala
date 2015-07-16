package com.spooky.inbound.step

import com.spooky.bittorrent.InfoHash
import spooky.util.ByteString
import com.spooky.inbound.PublicKeyBase
import com.spooky.inbound.ReceivePublicKey
import com.spooky.inbound.Reply
import com.spooky.inbound.InStep
import com.spooky.inbound.OutStep
import org.apache.commons.codec.binary.Hex
import com.spooky.bittorrent.BStrings

class InboundStep(infoHashes: List[InfoHash]) extends PublicKeyBase with InStep {

  //  def step(reply: Reply)(in: ByteString): Step = inbound(in)(reply)

  def step(in: ByteString): SendPublicKeyStep = {
//    println("in: " + Hex.encodeHexString(in.toArray))
    val (remotePublicKey, secretKey) = new ReceivePublicKey(keyAgreement).parse(in)
//    println("skey: " + Hex.encodeHexString(skey.raw))
//    println("remotePublicKey: " + Hex.encodeHexString(remotePublicKey.raw))
//    println("secretKey: " + Hex.encodeHexString(secretKey.raw))
//    println("publicKey: " + Hex.encodeHexString(publicKey.raw))
    new SendPublicKeyStep(infoHashes, publicKey, remotePublicKey, secretKey)
  }
}

object InboundStep {
  def main(args: Array[String]) {
    val is = new InboundStep(InfoHash.hex("597f6a218a58b0fe7880ba12466ccd89ca6c778f") :: Nil)

    is.step(ByteString(Hex.decodeHex("ee742163bac339e5945637792619b99b6497c67a953cadc547ae626ebf9fe3b9c021bf6c27103ee02e1386968d190343e5a62f32d6d1fb541254ab03dc2e3b1a1cec63043a1342620b4147cbe68c141f2110dcdc860f9eaff6e5410e21b35596c9358d288bbdcb9bbd020c1aa814025f1fe0afb3dd06db441f5db4981947ae2d".toCharArray())))
  }
}
