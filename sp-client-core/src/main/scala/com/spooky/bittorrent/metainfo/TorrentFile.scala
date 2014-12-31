package com.spooky.bittorrent.metainfo

case class TorrentFile(name: String, bytes: Int, md5: Option[Array[Byte]])