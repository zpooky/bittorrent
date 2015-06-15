package com.spooky.bittorrent.bencode

import org.scalatest.FunSuite


class BencodeTest extends FunSuite {
	def main(args: Array[String]) {
		val v = "i123e"
		println(v)
		println(Bencodes.decode(v))
	}

	test("dict2 strings") {
		println(Bencodes.decode("d1:w2:wa3:was4:wasde"))
	}
}
