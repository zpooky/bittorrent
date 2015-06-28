package com.spooky.mse;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHPublicKeySpec;

import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SecretKey;

public class BaseReceivePublicKey extends Base {
	private final KeyAgreement keyAgreement;

	public BaseReceivePublicKey(KeyAgreement keyAgreement) {
		this.keyAgreement = keyAgreement;
	}

	public Tuple<RemotePublicKey, SecretKey> parse(ByteBuffer require) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalStateException {
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
