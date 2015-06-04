package com.spooky.bittorrent.actors

import akka.actor.Actor
import com.spooky.bittorrent.model.TorrentSetup
import com.spooky.bittorrent.l.session.SessionManager
import com.spooky.bittorrent.model.EnrichedTorrentSetup
import com.spooky.bittorrent.l.file.FileInitiator2
import java.util.BitSet
import com.spooky.bittorrent.model.TorrentFileState

class TorrentStartActor extends Actor {
  def receive = {
    case TorrentSetup(torrent, root) => {
      //      val state = new FileInitiator2(torrent, root).state()
      println(s"${torrent.info.files.head}:${torrent.info.pieces.length}|piece${torrent.info.pieces.length / torrent.info.pieceLength}")
      val i = torrent.info.pieces.length
      val pl = torrent.info.pieceLength
      val size = torrent.info.pieces.length
      val b = new BitSet(size)
      for (i <- 0 until (size)) {
        b.set(i, true)
      }
      SessionManager.register(EnrichedTorrentSetup(torrent, root, TorrentFileState(b)))
      //        SessionManager.register(EnrichedTorrentSetup(torrent, root, state))
    }
  }
}
