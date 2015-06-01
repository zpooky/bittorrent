package com.spooky

import org.scalatest.FunSuite
import java.nio.charset.Charset
import com.spooky.rc4.Codec
import java.nio.Buffer
import java.nio.ByteBuffer
import org.apache.commons.codec.binary.Hex
import java.nio.ByteOrder
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.Sha1

class Rc4ScalaTest extends FunSuite {
  private val UTF8 = Charset.forName("UTF8")
  test("encrypt decrypt") {

    //    val bLocalSecret = LocalSecret.spooky

    //    val aSharedKey = SharedSecretKey(aLocalSecret, bPublicKey)
    //    val bSharedKey = SharedSecretKey(bLocalSecret, aPublicKey)
    //
    //    val aKey = ARc4Key(aSharedKey, skey)
    //    val bKey = ARc4Key(bSharedKey, skey)
    //
    //    val aRc4 = Rc4Stock(aKey, bKey)
    //    val bRc4 = Rc4Stock(bKey, aKey)
    //
    //    val plaintext = "spoooooky";
    //    val chiperText = aRc4.encrypt(plaintext)
    //    assert(chiperText !== plaintext)
    //    assert(new String(bRc4.encrypt(chiperText), UTF8) !== plaintext)
    //    assert(new String(aRc4.decrypt(chiperText), UTF8) !== plaintext)
    //    assert(new String(aRc4.encrypt(chiperText), UTF8) !== plaintext)
    //    assert(new String(bRc4.decrypt(chiperText), UTF8) === plaintext)
    //    assert(new String(bRc4.decrypt(chiperText), UTF8) === plaintext)

    val skey = InfoHash(Sha1.random.raw)

    val a = new AParty(skey)
    val b = new BParty(skey)

    b.receivePublic(a.sendPublic)
    a.receivePublic(b.sendPublic)

    println("----")
    b.receive(a.send)
    a.receive(b.send)

    b._3(a._3)

    println("----")
    b.receive(a.send)
    a.receive(b.send)

    a._4(ByteBuffer.wrap(b._4))

    println("----")
    b.receive(a.send)
    a.receive(b.send)

  }

  test("wtf") {

  }
}
