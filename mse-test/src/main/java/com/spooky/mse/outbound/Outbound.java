package com.spooky.mse.outbound;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.spooky.mse.PublicKeyBase;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.SKey;

public class Outbound extends PublicKeyBase {
	private final SKey skey;

	public Outbound(SKey skey) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		super();
		this.skey = skey;

	}

	public ReceivePublicKey sendPublicKey(Writer writer) throws Exception {
		ByteBuffer writeBuffer = send();
		writer.write(writeBuffer);

		return new ReceivePublicKey(skey, publicKey, keyAgreement);
	}

	private ByteBuffer send() {
		byte[] padding = getRandomPadding(PADDING_MAX / 2);
		byte[] dhPublicKeyBytes = publicKey.raw;

		ByteBuffer writeBuffer = ByteBuffer.allocate(dhPublicKeyBytes.length + padding.length);
		writeBuffer.put(dhPublicKeyBytes);
		writeBuffer.put(padding);

		writeBuffer.flip();

		return writeBuffer;
	}
}
