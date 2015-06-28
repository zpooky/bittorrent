package com.spooky.mse;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

import com.spooky.mse.o.LocalPublicKey;

public class PublicKeyBase extends Base {
	protected final LocalPublicKey publicKey;
	protected final KeyAgreement keyAgreement;

	public PublicKeyBase() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
		DHParameterSpec dh_param_spec = new DHParameterSpec(DH_P_BI, DH_G_BI, DH_L);
		KeyPairGenerator dh_key_generator = KeyPairGenerator.getInstance("DH");
		dh_key_generator.initialize(dh_param_spec);

		KeyPair key_pair = dh_key_generator.generateKeyPair();

		keyAgreement = KeyAgreement.getInstance("DH");

		keyAgreement.init(key_pair.getPrivate());

		DHPublicKey dh_public_key = (DHPublicKey) key_pair.getPublic();

		BigInteger dh_y = dh_public_key.getY();

		publicKey = new LocalPublicKey(bigIntegerToBytes(dh_y, DH_SIZE_BYTES));
	}
}
