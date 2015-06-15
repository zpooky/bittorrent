package com.spooky.bittorrent.mse

import java.security.MessageDigest
import com.spooky.bittorrent.data.SKey

sealed case class BRc4Key(override val raw: Array[Byte]) extends Rc4Key(raw)
object BRc4Key extends Base {
  def apply(shared: SharedSecretKey, skey: SKey): BRc4Key = {
    val digest = MessageDigest.getInstance("sha1")
    digest.update("keyB".getBytes(UTF8))
    digest.update(shared.raw)
    digest.update(skey.sum)
    BRc4Key(digest.digest())
  }
}
