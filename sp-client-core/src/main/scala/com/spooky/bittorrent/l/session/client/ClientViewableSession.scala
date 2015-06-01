package com.spooky.bittorrent.l.session.client

trait ClientViewableSession {
  def choking: Boolean
  def choked: Boolean
}
