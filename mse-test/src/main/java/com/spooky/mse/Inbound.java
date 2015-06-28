package com.spooky.mse;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.spec.DHPublicKeySpec;

import com.spooky.mse.io.Reader;
import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;

public class Inbound extends PublicKeyBase {

	private final SKey skey;

	public Inbound(SKey skey) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
		super();
		this.skey = skey;
	}

	public SendPublicKey publicKey(Reader reader) throws Exception {
		Tuple<RemotePublicKey, SecretKey> result = parse(reader.requireAtleast(publicKey.raw.length));
		// Tuple<RemotePublicKey, SecretKey> result =
		// parse(reader.requireExact(publicKey.raw.length + PADDING_MAX));

		RemotePublicKey remotePublicKey = result._1;
		SecretKey secret = result._2;
		return new SendPublicKey(skey, publicKey, keyAgreement, remotePublicKey, secret);
	}

	private Tuple<RemotePublicKey, SecretKey> parse(ByteBuffer require) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalStateException {
		byte[] buffer = require.array();
		BigInteger other_dh_y = bytesToBigInteger(buffer, 0, DH_SIZE_BYTES);

		KeyFactory dh_key_factory = KeyFactory.getInstance("DH");

		PublicKey otherPublicKey = dh_key_factory.generatePublic(new DHPublicKeySpec(other_dh_y, DH_P_BI, DH_G_BI));
		RemotePublicKey remotePublicKey = new RemotePublicKey(otherPublicKey);

		keyAgreement.doPhase(otherPublicKey, true);

		SecretKey secret = new SecretKey(keyAgreement.generateSecret());
		return new Tuple<>(remotePublicKey, secret);
	}
}
