package com.spooky.bittorrent.mse

import java.security.MessageDigest
import com.spooky.bittorrent.data.SKey

sealed case class ARc4Key(override val raw: Array[Byte]) extends Rc4Key(raw)
object ARc4Key extends Base {
  def apply(shared: SharedSecretKey, skey: SKey): ARc4Key = {
    val digest = MessageDigest.getInstance("sha1")
    digest.update("keyA".getBytes(UTF8))
    digest.update(shared.raw)
    digest.update(skey.raw)
    ARc4Key(digest.digest())
  }
}
