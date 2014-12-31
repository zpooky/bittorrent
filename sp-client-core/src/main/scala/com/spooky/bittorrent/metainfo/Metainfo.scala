package com.spooky.bittorrent.metainfo

trait Metainfo {
	def info: Array[Byte]
	def trackers: List[Tracker]
	def files: List[TorrentFile]
}