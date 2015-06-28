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
		ByteBuffer write_buffer = send();
		writer.write(write_buffer);

		return new ReceivePublicKey(skey, publicKey, keyAgreement);
	}

	private ByteBuffer send() {
		byte[] padding_a = getRandomPadding(PADDING_MAX / 2);
		byte[] dh_public_key_bytes = publicKey.raw;

		ByteBuffer write_buffer = ByteBuffer.allocate(dh_public_key_bytes.length + padding_a.length);
		write_buffer.put(dh_public_key_bytes);
		write_buffer.put(padding_a);

		write_buffer.flip();

		return write_buffer;
	}
}
