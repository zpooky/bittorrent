package com.spooky.mse.inbound;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.spooky.mse.BaseReceivePublicKey;
import com.spooky.mse.PublicKeyBase;
import com.spooky.mse.Tuple;
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
		Tuple<RemotePublicKey, SecretKey> result = new BaseReceivePublicKey(keyAgreement).parse(reader.requireAtleast(publicKey.raw.length));

		RemotePublicKey remotePublicKey = result._1;
		SecretKey secret = result._2;
		return new SendPublicKey(skey, publicKey, keyAgreement, remotePublicKey, secret);
	}

}
