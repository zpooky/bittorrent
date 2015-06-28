package com.spooky.mse.inbound;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.spooky.mse.Base;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.u.TransportCipher;

//ProtocolDecoderPHE
public class Confirm extends Base {

	private final MSEKeyPair mseKeyPair;

	public Confirm(MSEKeyPair mseKeyPair) {
		this.mseKeyPair = mseKeyPair;
	}

	public MSEKeyPair confirm(Writer writer) throws Exception {
		// 4 B->A: ENCRYPT(VC, crypto_select, len(padD), padD), ENCRYPT2(Payload Stream)
		TransportCipher writeCipher = mseKeyPair.writeCipher;

		byte[] padding = getRandomPadding(PADDING_MAX_NORMAL);
		ByteBuffer writeBuffer = ByteBuffer.allocate(VC.length + 4 + 2 + padding.length).order(ByteOrder.BIG_ENDIAN);

		writeBuffer.put(writeCipher.update(VC));
		writeBuffer.put(writeCipher.update(new byte[] { 0, 0, 0, CRYPTO_RC4 }));
		writeBuffer.put(writeCipher.update(new byte[] { (byte) (padding.length >> 8), (byte) padding.length }));
		if (writeBuffer.remaining() != padding.length) {
			throw new RuntimeException("not correct");
		}
		writeBuffer.put(writeCipher.update(padding));

		writeBuffer.flip();

		writer.write(writeBuffer);

		return mseKeyPair;
	}
}
