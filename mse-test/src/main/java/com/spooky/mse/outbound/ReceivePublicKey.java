package com.spooky.mse.outbound;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;

import org.apache.commons.codec.binary.Hex;

import com.spooky.mse.BaseReceivePublicKey;
import com.spooky.mse.Tuple;
import com.spooky.mse.io.Reader;
import com.spooky.mse.o.LocalPublicKey;
import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;

public class ReceivePublicKey extends BaseReceivePublicKey {

	private final SKey skey;
	private final LocalPublicKey publicKey;

	public ReceivePublicKey(SKey skey, LocalPublicKey publicKey, KeyAgreement keyAgreement) {
		super(keyAgreement);
		this.skey = skey;
		this.publicKey = publicKey;
	}

	public SendInfo receivePublicKey(Reader reader) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalStateException, Exception {
		System.out.println("receibv");
		ByteBuffer buffer = reader.requireAtleast(publicKey.raw.length);
		Tuple<RemotePublicKey, SecretKey> result = parse(buffer);
		RemotePublicKey remotePublicKey = result._1;
		SecretKey secretKey = result._2;

		System.out.println("remotePublicKey: " + Hex.encodeHexString(remotePublicKey.raw));
		System.out.println("secretKey: " + Hex.encodeHexString(secretKey.raw));
		System.out.println("publicKey: " + Hex.encodeHexString(publicKey.raw));

		return new SendInfo(skey, publicKey, remotePublicKey, secretKey);
	}
}
