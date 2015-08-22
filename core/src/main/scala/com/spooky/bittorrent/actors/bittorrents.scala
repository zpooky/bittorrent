package com.spooky.bittorrent.actors

import spooky.actor.Actor
import com.spooky.bittorrent.model.TorrentSetup
import com.spooky.bittorrent.l.session.Sessions
import com.spooky.bittorrent.model.EnrichedTorrentSetup
import com.spooky.bittorrent.l.file.FileInitiator2
import java.util.BitSet
import com.spooky.bittorrent.model.TorrentFileState
import com.spooky.bittorrent.model.TorrentStatistics
import com.spooky.bittorrent.protocol.server.CompositePeerProvider
import spooky.actor.Props

object TorrentStartActor {
  def props(actors: BittorrentActors, sessions: Sessions): Props = Props(classOf[TorrentStartActor], actors, sessions)
}

class TorrentStartActor(actors: BittorrentActors, sessions: Sessions) extends Actor {
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
        b.set(i + 1, true)
        b.set(i + 2, true)
        //        b.set(i+3, true)
        //        b.set(i+4, true)

      }
      val session = sessions.register(EnrichedTorrentSetup(torrent, root, TorrentFileState(b)))
      CompositePeerProvider(torrent, session)

    }
  }
}
