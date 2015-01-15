package com.spooky.bittorrent.l

import com.spooky.bittorrent.metainfo.Checksum
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.l.file.TorrentFileManager
import java.util.concurrent.ConcurrentHashMap
import com.spooky.bittorrent.metainfo.Torrent
import com.spooky.bittorrent.l.file.TorrentFileManager
import java.nio.file.Path
import java.nio.file.Paths
import com.spooky.bittorrent.model.TorrentRef
import com.spooky.bittorrent.model.TorrentSetup
import com.spooky.bittorrent.l.file.FileInitiator

object SessionManager {
  private val sessions = new ConcurrentHashMap[Checksum, SessionManager]
  def get(infoHash: Checksum): Option[SessionManager] = Option(sessions.get(infoHash))
  def register(setup: TorrentSetup): TorrentRef = {
    val torrent = setup.torrent
    val torrentFileState = FileInitiator(torrent, setup.root)
    val fileManager = new TorrentFileManager(torrent, setup.root)
    val peerId = PeerId.create
    sessions.put(torrent.infoHash, new SessionManager(fileManager, peerId))
    TorrentRef(torrent.infoHash, peerId)
  }
}
class SessionManager(val fileManager: TorrentFileManager, val peerId: PeerId) {
  @volatile var activeListeners: Int = 0
  @volatile var activeClients: Int = 0
  //announce peers
}
