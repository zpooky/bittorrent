package com.spooky.bittorrent.metainfo

import java.io.File
import com.spooky.bittorrent.bencode.TorrentFileStream
import com.spooky.bittorrent.bencode.Bencode
import com.spooky.bittorrent.bencode.BValue
import com.spooky.bittorrent.bencode.BDictionary
import com.spooky.bittorrent.bencode.BList
import com.spooky.bittorrent.bencode.BString
import com.spooky.bittorrent.bencode.BInteger
import com.spooky.bittorrent.bencode.BChecksum
import com.spooky.bittorrent.bencode.BInteger
import java.net.URL
import com.spooky.bittorrent.bencode.BDictionary
import com.spooky.bittorrent.bencode.BDictionary
import com.spooky.bittorrent.bencode.BList
import scala.annotation.tailrec

abstract class Algorithm {
  override def toString = this.getClass.getSimpleName.replace("$", "")
}
object Sha1 extends Algorithm
object Md5 extends Algorithm
sealed case class Checksum(sum: Array[Byte], algorithm: Algorithm)
case class Info(pieceLength: Int, length: Long, files: List[TorrentFile], pieces: List[Checksum], priv: Boolean, rootHash: Option[Checksum])
case class Node(host: String, port: Int)
case class Tracker(announce: String)
case class TorrentFile(name: String, bytes: Long)
case class Torrent(info: Info, nodes: List[Node], trackers: Set[Tracker], creationDate: Option[String], comment: Option[String], createdBy: Option[String], encoding: Option[String], httpSeeds: List[URL]) //extends Metainfo
object Torrent {
  def apply(torrent: File): Torrent = {
    val stream = TorrentFileStream(torrent)
    val dictionary = Bencode.decode(stream)
    //    println(dictionary)
    stream.close
    val torrentData = dictionary match {
      case (metaInfo: BDictionary) ⇒ {
        val infos = info(metaInfo.get("info").map(_.asInstanceOf[BDictionary]).get)
        Torrent(info = infos, nodes = nodes(metaInfo), trackers = announce(metaInfo), creationDate = creationDate(metaInfo), comment = comment(metaInfo), createdBy = createdBy(metaInfo), encoding = encoding(metaInfo), httpSeeds = httpSeeds(metaInfo))
      }
      case _ ⇒ throw new RuntimeException
    }
    torrentData
  }

  def info(metaInfo: BDictionary): Info = {
    val root = metaInfo.get("name").map({ case (value: BString) ⇒ value.value }).getOrElse("")
    val pieceLength = metaInfo.get("piece length").map { case (value: BInteger) ⇒ value.value }.map(_.toInt).getOrElse(0)
    val optionalLength = metaInfo.get("length").map { case (value: BInteger) ⇒ value.value }
    //TODO will fail if list is reversed
    val priv = metaInfo.get("private").map { case (value: BInteger) ⇒ value.value }.map(_ == 1l).getOrElse(false)
    val fileList = files(metaInfo, root, optionalLength)
    val length = optionalLength.getOrElse(fileList.foldLeft(0l)((first, current) ⇒ first + current.bytes))
    Info(pieceLength, length, fileList, checksums(metaInfo), priv, rootHash(metaInfo))
  }

  def files(metaInfo: BDictionary, root: String, length: Option[Long]): List[TorrentFile] = {
    metaInfo.get("files") match {
      case Some(files: BList) ⇒ {
        files.value.map { f ⇒ file(f.asInstanceOf[BDictionary], root) }
      }
      case Some(files) ⇒ throw new RuntimeException
      case None        ⇒ TorrentFile(root, length.get) :: Nil
    }
  }

  def file(metaInfo: BDictionary, root: String): TorrentFile = {
    val paths = metaInfo.get("path").map { case (l: BList) ⇒ l.value }
    val path = paths.map(l ⇒ l.map({ case (s: BString) ⇒ s.value }).foldLeft(StringBuilder.newBuilder)((builder, current) ⇒ builder ++= File.separator ++= current).toString).get
    TorrentFile(root +  path, metaInfo.get("length").map({ case (i: BInteger) ⇒ i.value }).get)
  }

  def checksums(metaInfo: BDictionary): List[Checksum] = {
    list(metaInfo.get("pieces")).map { case (checksum: BChecksum) ⇒ Checksum(checksum.value, Sha1) }
  }
  def rootHash(metaInfo: BDictionary): Option[Checksum] = {
    None
  }
  def nodes(metaInfo: BDictionary): List[Node] = {
    dictionary(metaInfo.get("nodes")).map { case (key: BString, value: BInteger) ⇒ Node(key.value, value.value.toInt) }
  }

  def announce(metaInfo: BDictionary): Set[Tracker] = {
    val announceList = list(metaInfo.get("announce")) ++ flatList(metaInfo.get("announce-list"))
    announceList.map({ case (announce: BString) ⇒ Tracker(announce.value) }).toSet
  }

  def creationDate(metaInfo: BDictionary) = {
    metaInfo.get("creation date").map { case (s: BInteger) ⇒ s.value + "" }
  }

  def comment(metaInfo: BDictionary) = {
    metaInfo.get("comment").map { case (s: BString) ⇒ s.value }
  }
  def createdBy(metaInfo: BDictionary) = {
    metaInfo.get("created by").map { case (s: BString) ⇒ s.value }
  }
  def encoding(metaInfo: BDictionary) = {
    metaInfo.get("encoding").map { case (s: BString) ⇒ s.value }
  }
  def httpSeeds(metaInfo: BDictionary) = {
    list(metaInfo.get("httpseeds")).map { case (url: BString) ⇒ new URL(url.value) }
  }

  def dictionary(value: Option[BValue]): List[Tuple2[BValue, BValue]] = value match {
    case Some(dictionary: BDictionary) ⇒ dictionary.value
    case Some(_)                       ⇒ throw new RuntimeException
    case None                          ⇒ Nil
  }
  def list(value: Option[BValue]): List[BValue] = value match {
    case Some(list: BList) ⇒ list.value
    case Some(value)       ⇒ List(value)
    case None              ⇒ Nil
  }
  def flatList(value: Option[BValue]): List[BValue] = value match {
    case Some(list: BList) ⇒ list.value.flatMap(value ⇒ flatList(Some(value)))
    case Some(value)       ⇒ List(value)
    case None              ⇒ Nil
  }

  def main(args: Array[String]): Unit = {
    val url = getClass.getResource("/debian.torrent")
    val file = new File(url.toURI())
//    println(Torrent(file))
    new File("D:\\torrent\\").listFiles().filter { x ⇒ x.isFile() }.filter { x ⇒ x.getName.endsWith(".torrent") }.foreach { x ⇒ Torrent(x) }
  }
}
