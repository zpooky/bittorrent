package com.spooky.inbound

import javax.crypto.spec.DHParameterSpec
import java.math.BigInteger
import java.security.KeyPairGenerator
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPublicKey
import org.apache.commons.codec.binary.Hex

abstract class PublicKeyBase extends Base {
  protected val p = new java.math.BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563", 16)

  private val dhParamSpec = new DHParameterSpec(DH_P_BI, DH_G_BI, DH_L)
  val dh_key_generator = KeyPairGenerator.getInstance("DH")
  dh_key_generator.initialize(dhParamSpec)

  private val keyPair = dh_key_generator.generateKeyPair()

  val keyAgreement = KeyAgreement.getInstance("DH")
  keyAgreement.init(keyPair.getPrivate())

  private val dhPublicKey = keyPair.getPublic.asInstanceOf[DHPublicKey]
  private val dh_y = dhPublicKey.getY()

  println("New: " + LocalPublicKey.parse(dh_y, DH_SIZE_BYTES))
  println("Old: " + Hex.encodeHexString(bigIntegerToBytes(dh_y, DH_SIZE_BYTES)))

  val publicKey = LocalPublicKey(bigIntegerToBytes(dh_y, DH_SIZE_BYTES))

  def decodeString(str: String): Array[Byte] = {
    val chars = str.toCharArray();

    val chars_length = chars.length - chars.length % 2;

    val res = Array.ofDim[Byte](chars_length / 2);

    for (i <- 0.until(chars_length, 2)) {

      val b = new String(chars, i, 2);

      res(i / 2) = Integer.parseInt(b, 16).asInstanceOf[Byte];
    }

    return (res);
  }

  def bigIntegerToBytes(bi: BigInteger, num_bytes: Int): Array[Byte] = {
    var str = bi.toString(16);
    // System.out.println(str.length() + "|" + str);
    while (str.length() < num_bytes * 2) {
      str = "0" + str;
    }
    // System.out.println(num_bytes * 2 + "||" + str);
    return (decodeString(str));
  }
}
