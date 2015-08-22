package com.spooky.bittorrent.l.session

import com.spooky.bittorrent.l.file.TorrentFileManager
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.InfoHash
import java.util.concurrent.ConcurrentHashMap
import com.spooky.bittorrent.l.session.client.ClientSession
import java.util.function.Function
import com.spooky.cipher.MSEKeyPair
import com.spooky.bittorrent.model.TorrentStatistics
import spooky.actor.ActorRef

class Session(val fileManager: TorrentFileManager, val peerId: PeerId, val announce: ActorRef) extends ViewableSession {
	private val torrent = fileManager.torrent
  private val infoHash: InfoHash = fileManager.torrent.infoHash
  private val clients = new ConcurrentHashMap[PeerId, ClientSession]
  @volatile private var activeListeners: Int = 0
  @volatile private var activeClients: Int = 0



  def statistics: TorrentStatistics = {
    TorrentStatistics(torrent.infoHash, torrent.info.length, torrent.info.length, 0, 0)
  }

  def init(peerId: PeerId, keyPair: MSEKeyPair): ClientSession = { //not used
    clients.put(peerId, new ClientSession(peerId, keyPair))
  }
  //announce peers

}
