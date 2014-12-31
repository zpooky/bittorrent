package com.spooky.bittorrent.metainfo

abstract class Algorithm
object Sha1 extends Algorithm
object Md5 extends Algorithm
sealed case class Checksum(sum: Array[Byte], algorithm: Algorithm)