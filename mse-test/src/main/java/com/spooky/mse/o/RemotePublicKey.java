package com.spooky.mse.o;

import java.security.PublicKey;

public class RemotePublicKey extends ArrayWrapper {

	public RemotePublicKey(byte[] raw) {
		super(raw);
		System.out.println(this);
	}

	public RemotePublicKey(PublicKey pk) {
		this(pk.getEncoded());
	}

}
