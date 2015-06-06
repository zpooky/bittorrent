package com.spooky.bittorrent

import java.nio.channels.FileChannel
import java.io.File
import java.nio.file.StandardOpenOption
import scala.util.Random
import java.nio.ByteBuffer
import com.spooky.bittorrent.u.GigaByte

object RandomFile {
  def main(args: Array[String]): Unit = {
    val channel = FileChannel.open(new File("O:\\tmp\\file.dump").toPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    val r = new Random(0)
    val buffer = ByteBuffer.allocate(1024 * 4)
    val bytes = GigaByte(50).toBytes
    println(bytes.capacity.toLong)
    for (_ <- 0l to(bytes.capacity.toLong, buffer.capacity.toLong)) {
      r.nextBytes(buffer.array())
      buffer.limit(buffer.capacity)
      buffer.position(0)
      channel.write(buffer)
    }
    channel.close()
  }
}
