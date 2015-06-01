package com.spooky.bittorrent.metainfo

import com.spooky.bittorrent.Checksum
import com.spooky.bittorrent.InfoHash

trait Metainfo {
	def infoHash: InfoHash
//	def trackers: List[Tracker]
//	def files: List[TorrentFile]
}
