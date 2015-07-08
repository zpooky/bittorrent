package com.spooky.cipher

sealed case class MSEKeyPair(writeCipher: WriteCipher, readCipher: ReadCipher)
