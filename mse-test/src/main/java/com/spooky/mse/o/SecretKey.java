package com.spooky.mse.o;

public class SecretKey extends ArrayWrapper {

	public SecretKey(byte[] raw) {
		super(raw);
		System.out.println(this);
	}

}
