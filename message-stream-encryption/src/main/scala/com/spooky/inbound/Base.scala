package com.spooky.inbound

import java.math.BigInteger
import java.util.concurrent.ThreadLocalRandom
import java.nio.charset.Charset

abstract class Base {
  protected val DH_P = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563"
  protected val DH_G = "02"
  protected val DH_L = 160

  protected val UTF8 = Charset.forName("UTF8");
  protected val KEYA_IV = "keyA".getBytes(UTF8)
  protected val KEYB_IV = "keyB".getBytes(UTF8)
  protected val REQ1_IV = "req1".getBytes(UTF8)
  protected val REQ2_IV = "req2".getBytes(UTF8)
  protected val REQ3_IV = "req3".getBytes(UTF8)
  protected val VC = Array[Byte](0, 0, 0, 0, 0, 0, 0, 0)

  protected val RC4_STREAM_ALG = "RC4";

  protected val DH_P_BI = new BigInteger(DH_P, 16)
  protected val DH_G_BI = new BigInteger(DH_G, 16)

  protected val DH_SIZE_BYTES = DH_P.length() / 2

  protected val CRYPTO_RC4: Byte = 0x02

  protected val PADDING_MAX = 512

  private def randomPaddingSize: Int = {
    val random = ThreadLocalRandom.current
    Math.abs(random.nextInt % PADDING_MAX)
  }

  protected def randomPadding(): Array[Byte] = {
    val padding = Array.ofDim[Byte](randomPaddingSize)
    val random = ThreadLocalRandom.current
    random.nextBytes(padding)
    padding
  }
}
