package com.spooky.inbound

import javax.crypto.KeyAgreement
import spooky.util.ByteString
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import javax.crypto.spec.DHPublicKeySpec
import org.apache.commons.codec.binary.Hex

class ReceivePublicKey(keyAgreement: KeyAgreement) extends Base {
  def parse(s: ByteString): Tuple2[RemotePublicKey, SecretKey] = {
    val otherDHY = new BigInteger(Hex.encodeHexString(s.take(96).toArray), 16)
    println("xther public key: " + Hex.encodeHexString(s.take(96).toArray))
    println("other public key: " + bytesToBigInteger(s, 0, DH_SIZE_BYTES))

//    val otherDHY = bytesToBigInteger(s, 0, DH_SIZE_BYTES);

    val dhKeyFactory = KeyFactory.getInstance("DH")

    val otherPublicKey = dhKeyFactory.generatePublic(new DHPublicKeySpec(otherDHY, DH_P_BI, DH_G_BI))
    val remotePublicKey = RemotePublicKey(otherPublicKey)

    keyAgreement.doPhase(otherPublicKey, true)

    val secretKey = SecretKey(keyAgreement.generateSecret)
    (remotePublicKey, secretKey)
  }

  def bytesToBigInteger(s: ByteString, arg: Int, DH_SIZE_BYTES: Int) = {
    new BigInteger(encodeString(s, arg, DH_SIZE_BYTES), 16)
  }

  def encodeString(bytes: ByteString, offset: Int, len: Int): String = {
    val x = Array.ofDim[Byte](len)

    System.arraycopy(bytes.toArray, offset, x, 0, len);

    nicePrint(x, true)
  }

  def nicePrint(data: Array[Byte], tight: Boolean): String = {
    nicePrint(data, tight, 1024)
  }

  val HEXDIGITS: Array[Char] = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');

  def nicePrint(data: Array[Byte], tight: Boolean, max_length: Int): String = {
    if (data == null) {
      return "";
    }

    var dataLength = data.length;

    if (dataLength > max_length) {
      dataLength = max_length;
    }

    var size = dataLength * 2;
    if (!tight) {
      size = size + (dataLength - 1) / 4;
    }

    val out = Array.ofDim[Char](size);

    var pos = 0;

    for (i <- 0 until dataLength) {
      if ((!tight) && (i % 4 == 0) && i > 0) {
        out(pos) = ' '
        pos = pos + 1
      }

      out(pos) = HEXDIGITS(((data(i) >> 4) & 0xF).asInstanceOf[Byte]);
      pos = pos + 1
      out(pos) = HEXDIGITS((data(i) & 0xF).asInstanceOf[Byte]);
      pos = pos + 1
    }

//    System.out.println("other public: " + new String(out));
    return new String(out)
  }

}
