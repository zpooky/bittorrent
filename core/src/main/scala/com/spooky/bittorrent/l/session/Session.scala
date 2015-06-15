package com.spooky.bittorrent.l.session

import com.spooky.bittorrent.l.file.TorrentFileManager
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.l.session.client.ClientSession
import java.util.concurrent.ConcurrentHashMap
import com.spooky.bittorrent.l.session.client.ClientSession
import java.util.function.Function

class Session(val fileManager: TorrentFileManager, val peerId: PeerId) extends ViewableSession {
 private val infoHash: InfoHash = fileManager.torrent.infoHash
 private val clients = new ConcurrentHashMap[PeerId,ClientSession]
  @volatile private var activeListeners: Int = 0
  @volatile private var activeClients: Int = 0

  def get(peerId: PeerId): ClientSession = {
    clients.computeIfAbsent(peerId, new Function[PeerId, ClientSession] {
      def apply(p: PeerId): ClientSession = throw new NotImplementedError//new ClientSession //TODO
    })
  }
  //announce peers

}
