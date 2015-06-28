package com.spooky.mse;

import java.nio.ByteBuffer;

import javax.crypto.KeyAgreement;

import com.spooky.mse.io.Reader;
import com.spooky.mse.o.LocalPublicKey;
import com.spooky.mse.o.SKey;

public class ReceivePublicKey {

	private final SKey skey;
	private final LocalPublicKey publicKey;
	private final KeyAgreement keyAgreement;

	public ReceivePublicKey(SKey skey, LocalPublicKey publicKey, KeyAgreement keyAgreement) {
		this.skey = skey;
		this.publicKey = publicKey;
		this.keyAgreement = keyAgreement;
	}

	public SendInfo receivePublicKey(Reader reader) {
		ByteBuffer read_buffer = ByteBuffer.allocate(publicKey.raw.length);

		return new SendInfo();
	}

}
