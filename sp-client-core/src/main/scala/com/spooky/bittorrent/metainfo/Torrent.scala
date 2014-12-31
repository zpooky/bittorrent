package com.spooky.bittorrent.metainfo

import java.io.File
import scala.io.Source

case class Torrent(info: Checksum, trackers: List[Tracker], files: List[TorrentFile], creationDate: Option[String], comment: Option[String], createdBy: Option[String], encoding: Option[String]) //extends Metainfo
object Torrent {
	def apply(torrentFile: File) = {
		val source = Source.fromFile(torrentFile)
		source.ch
		null
	}
	def main(args: Array[String]) {
		val source = Source.fromFile(new File("/local/haskell/workspace/torrent/debian.torrent"))
		println(source.ch)
	}
}