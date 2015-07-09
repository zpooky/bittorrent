package com.spooky.bittorrent.l.session

import com.spooky.bittorrent.l.file.TorrentFileManager
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.InfoHash
import java.util.concurrent.ConcurrentHashMap
import com.spooky.bittorrent.l.session.client.ClientSession
import java.util.function.Function
import com.spooky.cipher.MSEKeyPair

class Session(val fileManager: TorrentFileManager, val peerId: PeerId) extends ViewableSession {
  private val infoHash: InfoHash = fileManager.torrent.infoHash
  private val clients = new ConcurrentHashMap[PeerId, ClientSession]
  @volatile private var activeListeners: Int = 0
  @volatile private var activeClients: Int = 0

  def init(peerId: PeerId, keyPair: MSEKeyPair): ClientSession = {
    clients.computeIfAbsent(peerId, new Function[PeerId, ClientSession] {
      def apply(p: PeerId): ClientSession = new ClientSession(peerId, keyPair) //TODO
    })
  }
  //announce peers

}
