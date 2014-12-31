package com.spooky.bittorrent.peer

import com.spooky.bittorrent.metainfo.Metainfo

class CompositePeerProvider {
	def get(count: Int): List[Peer] = null
}

object CompositePeerProvider {
	def apply(metaInfo: Metainfo, provider: List[Provider]): CompositePeerProvider = new CompositePeerProvider
}