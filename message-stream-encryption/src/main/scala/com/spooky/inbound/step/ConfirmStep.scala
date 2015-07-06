package com.spooky.inbound.step

import com.spooky.inbound.OutStep
import com.spooky.inbound.InStep
import com.spooky.inbound.Base
import com.spooky.inbound.Reply
import akka.util.ByteString
import com.spooky.inbound.OutStep
import com.spooky.cipher.ReadRC4Cipher
import com.spooky.cipher.WriteRC4Cipher
import java.nio.ByteBuffer
import java.nio.ByteOrder
import akka.util.FakeBStrings
import com.spooky.inbound.CryptoProvider
import com.spooky.cipher.WriteCipher
import com.spooky.cipher.ReadCipher
import com.spooky.inbound.RC4
import com.spooky.inbound.Plain
import com.spooky.cipher.WritePlain
import com.spooky.cipher.ReadPlain

class ConfirmStep(readCipher: ReadRC4Cipher, writeCipher: WriteRC4Cipher, cryptoProvider: CryptoProvider) extends Base with OutStep {

  def step(reply: Reply): InStep = {

    val padding = randomPadding()
    val writeBuffer = ByteBuffer.allocate(VC.length + 4 + 2 + padding.length).order(ByteOrder.BIG_ENDIAN);

    writeBuffer.put(writeCipher.update(VC));
    writeBuffer.put(writeCipher.update(Array[Byte](0, 0, 0, CRYPTO_RC4)))
    writeBuffer.put(writeCipher.update(Array[Byte]((padding.length >> 8).asInstanceOf[Byte], padding.length.asInstanceOf[Byte])))
    if (writeBuffer.remaining() != padding.length) {
      throw new RuntimeException("not correct");
    }
    writeBuffer.put(writeCipher.update(padding));

    writeBuffer.flip();

    reply.reply(FakeBStrings(writeBuffer))

    val (chosenWriteCipher, chosenReadCipher) = choose(cryptoProvider, writeCipher, readCipher)
    new DoneStep(chosenWriteCipher, chosenReadCipher)
  }

  private def choose(cryptoProvider: CryptoProvider, writeCipher: WriteRC4Cipher, readCipher: ReadRC4Cipher): Tuple2[WriteCipher, ReadCipher] = cryptoProvider match {
    case RC4   => (writeCipher, readCipher)
    case Plain => (WritePlain, ReadPlain)
    case c     => throw new RuntimeException(s"Unknown crypto select: $c")
  }

}
