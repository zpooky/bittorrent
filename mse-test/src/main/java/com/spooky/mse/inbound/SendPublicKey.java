package com.spooky.mse.inbound;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.spooky.mse.Base;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.LocalPublicKey;
import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;

//ProtocolDecoderPHE
public class SendPublicKey extends Base {

	private final SKey skey;
	private final LocalPublicKey publicKey;
	private final RemotePublicKey remotePublicKey;
	private final SecretKey secret;

	public SendPublicKey(SKey skey, LocalPublicKey publicKey, RemotePublicKey remotePublicKey, SecretKey secret) {
		this.skey = skey;
		this.publicKey = publicKey;
		this.remotePublicKey = remotePublicKey;
		this.secret = secret;
	}

	public ReceiveInfo sendPublicKey(Writer writer) throws Exception {
		write(writer);
		return new ReceiveInfo(skey, publicKey, remotePublicKey, secret);
	}

	private void write(Writer writer) throws Exception {
		byte[] padding_b = getRandomPadding(PADDING_MAX_NORMAL / 2);
		byte[] rawPublicKey = publicKey.raw;

		ByteBuffer buffer = ByteBuffer.allocate(rawPublicKey.length + padding_b.length).order(ByteOrder.BIG_ENDIAN);

		buffer.put(rawPublicKey);
		buffer.put(padding_b);

		buffer.flip();
		writer.write(buffer);
	}
}
