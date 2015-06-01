package com.spooky.bencode

import java.nio.ByteBuffer

object wtf {

	object %:: {
		def unapply(xs: String): Option[(Char, String)] = if (xs.isEmpty) None else Some((xs.head, xs.tail))
		def unapply(xs: BStream): Option[(Char, BStream)] = if (xs.isEmpty) None else Some((xs.headChar, xs.tail))
//    def unapply(xs: ByteBuffer):Option[(,)]
	}
}
