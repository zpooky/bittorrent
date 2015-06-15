package com.spooky.bittorrent.metainfo

import java.io.File
import java.net.URL
import scala.annotation.tailrec
import java.util.Base64
import java.net.URLEncoder
import org.apache.commons.codec.net.URLCodec
import java.nio.charset.Charset
import java.nio.ByteBuffer
import java.security.MessageDigest
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import org.apache.commons.codec.binary.Hex
import java.util.Arrays
import com.spooky.bencode.TorrentFileStream
import com.spooky.bittorrent.Checksum
import com.spooky.bencode._
import com.spooky.bittorrent.Sha1
import com.spooky.bittorrent.InfoHash
import com.spooky.bittorrent.InfoHashs

object Torrents {
  def apply(torrent: File): Torrent = {
    val stream = TorrentFileStream(torrent)
    //		println(torrent)1
    //    println(stream.toString)
    val dictionary = Bencode.decode(stream)
    val infoHash = InfoHashs.hash(stream)
    //    println(dictionary)
    stream.close
    val torrentData = dictionary match {
      case (metaInfo: BDictionary) => {
        val infos = info(metaInfo.get("info").map(_.asInstanceOf[BDictionary]).get)
        Torrent(infoHash = infoHash, info = infos, nodes = nodes(metaInfo), trackers = announce(metaInfo), creationDate = creationDate(metaInfo), comment = comment(metaInfo), createdBy = createdBy(metaInfo), encoding = encoding(metaInfo), httpSeeds = httpSeeds(metaInfo))
      }
      case _ => throw new RuntimeException
    }
    //    println(torrentData)
    torrentData
  }

  private def info(metaInfo: BDictionary): Info = {
    val root = metaInfo.get("name").map({ case (value: BString) => value.value }).getOrElse("")
    val pieceLength = metaInfo.get("piece length").map { case (value: BInteger) => value.value }.map(_.toInt).getOrElse(0)
    val optionalLength = metaInfo.get("length").map { case (value: BInteger) => value.value }
    val priv = metaInfo.get("private").map { case (value: BInteger) => value.value }.map(_ == 1l).getOrElse(false)
    val fileList = files(metaInfo, root, optionalLength)
    val length = optionalLength.getOrElse(fileList.foldLeft(0l)((first, current) => first + current.bytes))
    Info(pieceLength, length, fileList, checksums(metaInfo), priv, rootHash(metaInfo))
  }

  private def files(metaInfo: BDictionary, root: String, length: Option[Long]): List[TorrentFile] = {
    metaInfo.get("files") match {
      case Some(files: BList) => {
        files.value.map { f => file(f.asInstanceOf[BDictionary], root) }
      }
      case Some(files) => throw new RuntimeException
      case None        => TorrentFile(root, length.get) :: Nil
    }
  }

  private def file(metaInfo: BDictionary, root: String): TorrentFile = {
    val paths = metaInfo.get("path").map { case (l: BList) => l.value }
    val path = paths.map(l => l.map({ case (s: BString) => s.value }).foldLeft(StringBuilder.newBuilder)((builder, current) => builder ++= File.separator ++= current).toString).get
    TorrentFile(root + path, metaInfo.get("length").map({ case (i: BInteger) => i.value }).get)
  }

  private def checksums(metaInfo: BDictionary): List[Checksum] = {
    list(metaInfo.get("pieces")).map { case (checksum: BChecksum) => Checksum(checksum.value, Sha1) }
  }
  private def rootHash(metaInfo: BDictionary): Option[Checksum] = {
    None
  }
  private def nodes(metaInfo: BDictionary): List[Node] = {
    dictionary(metaInfo.get("nodes")).map { case (key: BString, value: BInteger) => Node(key.value, value.value.toInt) }
  }

  private def announce(metaInfo: BDictionary): Set[Tracker] = {
    val announceList = list(metaInfo.get("announce")) ++ flatList(metaInfo.get("announce-list"))
    announceList.map({ case (announce: BString) => Tracker(announce.value) }).toSet
  }

  private def creationDate(metaInfo: BDictionary) = {
    metaInfo.get("creation date").map { case (s: BInteger) => s.value + "" }
  }

  private def comment(metaInfo: BDictionary) = {
    metaInfo.get("comment").map { case (s: BString) => s.value }
  }
  private def createdBy(metaInfo: BDictionary) = {
    metaInfo.get("created by").map { case (s: BString) => s.value }
  }
  private def encoding(metaInfo: BDictionary) = {
    metaInfo.get("encoding").map { case (s: BString) => s.value }
  }
  private def httpSeeds(metaInfo: BDictionary) = {
    list(metaInfo.get("httpseeds")).map { case (url: BString) => new URL(url.value) }
  }

  private def dictionary(value: Option[BValue]): List[Tuple2[BValue, BValue]] = value match {
    case Some(dictionary: BDictionary) => dictionary.value
    case Some(_)                       => throw new RuntimeException
    case None                          => Nil
  }
  private def list(value: Option[BValue]): List[BValue] = value match {
    case Some(list: BList) => list.value
    case Some(value)       => List(value)
    case None              => Nil
  }
  private def flatList(value: Option[BValue]): List[BValue] = value match {
    case Some(list: BList) => list.value.flatMap(value => flatList(Some(value)))
    case Some(value)       => List(value)
    case None              => Nil
  }

  def main(args: Array[String]): Unit = {
    val url = getClass.getResource("/debian.torrent")
    val file = new File(url.toURI())
    val t = Torrents(new File("D:\\torrent\\Community.S05E01.HDTV.x264-LOL.mp4.torrent"))
    //    println(Base64.getEncoder.encodeToString(t.infoHash.sum))
    println("%40%CA%3F%3E%28%EA_%89%F3%29qv%D7%813%08R%9E%12%1A")
    val codec = new URLCodec
    println(new String(codec.encode(t.infoHash.sum), Charset.forName("ASCII")))
    println(codec.encode(new String(t.infoHash.sum, Charset.forName("UTF8"))))
    println(codec.encode(new String(t.infoHash.sum, Charset.forName("ASCII"))))
    println(codec.encode(new String(t.infoHash.sum, Charset.forName("UTF16"))))
    //    new File("D:\\torrent\\").listFiles().filter { x => x.isFile() }.filter { x => x.getName.endsWith(".torrent") }.foreach { x => Torrent(x) }
  }
}
