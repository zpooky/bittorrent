package com.spooky.bittorrent.l.session

import scala.collection.JavaConversions._
import com.spooky.bittorrent.model.PeerId
import com.spooky.bittorrent.l.file.TorrentFileManager
import java.util.concurrent.ConcurrentHashMap
import com.spooky.bittorrent.model.TorrentSetup
import com.spooky.bittorrent.Checksum
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.model.EnrichedTorrentSetup
import spooky.actor.ActorSystem
import com.spooky.bittorrent.actors.AnnounceActor

class Sessions(actorSystem: ActorSystem) {
  private val sessions = new ConcurrentHashMap[InfoHash, Session]
  def get(infoHash: InfoHash): Option[Session] = Option(sessions.get(infoHash))
  def register(setup: EnrichedTorrentSetup): Session = setup match {
    case EnrichedTorrentSetup(torrent, root, state) => {
      val session = get(torrent.infoHash)
      session.getOrElse({
        val fileManager = TorrentFileManager(torrent, root, state)
        val peerId = PeerId.create
        val session = new Session(fileManager, peerId, actorSystem.actorOf(AnnounceActor.props))
        sessions.put(torrent.infoHash, session)
        session
      })
    }
  }
  def infoHashes: List[InfoHash] = sessions.keys().toList
}
