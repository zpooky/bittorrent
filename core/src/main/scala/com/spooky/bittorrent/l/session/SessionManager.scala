package com.spooky.bittorrent.l.session

import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.l.file.TorrentFileManager
import java.util.concurrent.ConcurrentHashMap
import com.spooky.bittorrent.model.TorrentRef
import com.spooky.bittorrent.model.TorrentSetup
import com.spooky.bittorrent.l.file.FileInitiator2
import com.spooky.bittorrent.Checksum
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.model.EnrichedTorrentSetup

object SessionManager {
  private val sessions = new ConcurrentHashMap[InfoHash, Session]
  def get(infoHash: InfoHash): Option[Session] = Option(sessions.get(infoHash))
  def test: Session = sessions.values().toArray()(0).asInstanceOf[Session]
  def register(setup: EnrichedTorrentSetup): TorrentRef = setup match {
    case EnrichedTorrentSetup(torrent, root, state) => {
      //    println(torrentFileState.getDownloaded(torrent))
      val fileManager = TorrentFileManager(torrent, root, state)
      val peerId = PeerId.create
      sessions.put(torrent.infoHash, new Session(fileManager, peerId))
      TorrentRef(torrent, peerId)
    }
  }
}
