package com.spooky.bittorrent.metainfo

trait Metainfo {
	def infoHash: Checksum
//	def trackers: List[Tracker]
//	def files: List[TorrentFile]
}