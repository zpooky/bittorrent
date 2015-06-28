package com.spooky.mse.o;

import java.security.PublicKey;

public class RemotePublicKey extends ArrayWrapper {

	public RemotePublicKey(byte[] raw) {
		super(raw);
	}

	public RemotePublicKey(PublicKey pk) {
		super(pk.getEncoded());
	}

}
