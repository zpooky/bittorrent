package com.spooky.mse.o;

import com.spooky.mse.u.TransportCipher;

public class MSEKeyPair {
	public final TransportCipher readCipher;
	public final TransportCipher writeCipher;

	public MSEKeyPair(TransportCipher readCipher, TransportCipher writeCipher) {
		this.readCipher = readCipher;
		this.writeCipher = writeCipher;
	}

}
