package com.spooky.bittorrent.protocol.server

import com.spooky.bittorrent.model.Peer
import com.spooky.bittorrent.metainfo.Metainfo

class CompositePeerProvider {
	def get(count: Int): List[Peer] = null
}

object CompositePeerProvider {
	def apply(metaInfo: Metainfo, provider: List[Provider]): CompositePeerProvider = new CompositePeerProvider
}
