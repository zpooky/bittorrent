package com.spooky

import java.nio.channels.SocketChannel
import java.net.SocketAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.PublicKey
import java.nio.charset.Charset

object ClientTest {
  def main(args: Array[String]): Unit = {
        val address = new InetSocketAddress("localhost", 26555)
//    val address = new InetSocketAddress("localhost", 10112)
    val channel = SocketChannel.open(address)
    //
    val a = new AParty(InfoHash.hex("597f6a218a58b0fe7880ba12466ccd89ca6c778f"))
    //
    val writeBuffer = ByteBuffer.allocate(1024).order(ByteOrder.BIG_ENDIAN)
    writeBuffer.put(a.sendPublic.raw)
    channel.write(writeBuffer.flip().asInstanceOf[ByteBuffer])
    //
    a.receivePublic(PublicKey.raw(read(channel)))
    //
    writeBuffer.clear()
    writeBuffer.put(a._3)
    channel.write(writeBuffer.flip().asInstanceOf[ByteBuffer])
    //
    a._4(read(channel))
    //
    writeBuffer.clear()
    writeBuffer.put("Test data".getBytes(Charset.forName("UTF8")))
    channel.write(writeBuffer.flip().asInstanceOf[ByteBuffer])
  }
  def read(channel: SocketChannel): ByteBuffer = {
    Thread.sleep(1000)
    val readBuffer = ByteBuffer.allocate(1024).order(ByteOrder.BIG_ENDIAN)
    channel.read(readBuffer)
    println("readBuffer:"+readBuffer)
    readBuffer.flip().asInstanceOf[ByteBuffer]
  }
}
