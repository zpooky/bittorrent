package com.spooky.mse.o;

import java.nio.ByteBuffer;

import com.spooky.mse.Base;
import com.spooky.mse.io.Reader;
import com.spooky.mse.u.TransportCipher;

public class MSEKeyPair extends Base {

	public final TransportCipher readCipher;
	public final TransportCipher writeCipher;

	public MSEKeyPair(TransportCipher readCipher, TransportCipher writeCipher) {
		this.readCipher = readCipher;
		this.writeCipher = writeCipher;
	}

	public MSEKeyPair debug(Reader reader) throws Exception {
		ByteBuffer buffer = reader.requireAtleast(1);
		ByteBuffer decoded = readCipher.update(buffer.duplicate());
		System.out.print("decoded: ");
		print(decoded);
		return this;
	}

	public void print(ByteBuffer bb) {
		while (bb.hasRemaining()) {
			System.out.print((char) bb.get());
		}
		System.out.println();
	}

}
