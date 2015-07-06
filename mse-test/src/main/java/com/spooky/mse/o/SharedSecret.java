package com.spooky.mse.o;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class SharedSecret extends ArrayWrapper {

	public SharedSecret(byte[] raw) {
		super(raw);
		System.out.println(this);
	}

	public static SharedSecret fromHex(String hex) throws DecoderException {
		return new SharedSecret(Hex.decodeHex(hex.toCharArray()));

	}
}
