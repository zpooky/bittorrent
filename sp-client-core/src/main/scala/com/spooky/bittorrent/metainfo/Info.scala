package com.spooky.bittorrent.metainfo

case class Info(pieceLength: Int, pieces: List[Checksum], priv: Boolean)