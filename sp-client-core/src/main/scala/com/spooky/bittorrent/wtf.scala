package com.spooky.bittorrent

object wtf {

	object %:: {
		def unapply(xs: String): Option[(Char, String)] = if (xs.isEmpty) None else Some((xs.head, xs.tail))
	}
}