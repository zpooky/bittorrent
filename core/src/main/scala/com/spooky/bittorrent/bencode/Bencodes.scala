package com.spooky.bittorrent.bencode

import spray.http.HttpData
import com.spooky.bencode.BValue
import com.spooky.bencode.BaseBencode

object Bencodes extends BaseBencode {

  def decode(stream: HttpData.NonEmpty): BValue = {
    decode(HttpDataBStream(stream))
  }

}
