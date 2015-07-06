package com.spooky.mse.o;

public class LocalPublicKey extends ArrayWrapper {

	public LocalPublicKey(byte[] raw) {
		super(raw);
		System.out.println(this);
	}

}
