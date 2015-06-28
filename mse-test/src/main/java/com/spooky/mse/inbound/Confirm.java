package com.spooky.mse.inbound;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.spooky.mse.Base;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.u.TransportCipher;

public class Confirm extends Base {

	private final MSEKeyPair mseKeyPair;

	public Confirm(MSEKeyPair mseKeyPair) {
		this.mseKeyPair = mseKeyPair;
	}

	public MSEKeyPair confirm(Writer writer) throws Exception {
		// 4 B->A: ENCRYPT(VC, crypto_select, len(padD), padD), ENCRYPT2(Payload Stream)
		TransportCipher writeCipher = mseKeyPair.writeCipher;

		byte[] padding = getRandomPadding(PADDING_MAX_NORMAL);
		ByteBuffer write_buffer = ByteBuffer.allocate(VC.length + 4 + 2 + padding.length).order(ByteOrder.BIG_ENDIAN);

		write_buffer.put(writeCipher.update(VC));
		write_buffer.put(writeCipher.update(new byte[] { 0, 0, 0, CRYPTO_RC4 }));
		write_buffer.put(writeCipher.update(new byte[] { (byte) (padding.length >> 8), (byte) padding.length }));
		if (write_buffer.remaining() != padding.length) {
			throw new RuntimeException("not correct");
		}
		write_buffer.put(writeCipher.update(padding));

		write_buffer.flip();

		writer.write(write_buffer);

		return mseKeyPair;
	}
}
