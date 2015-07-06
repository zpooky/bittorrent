package com.spooky.mse.o;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class SKey extends ArrayWrapper {

	private SKey(byte[] raw) {
		super(raw);
		System.out.println(this);
	}

	public static SKey fromHex(String hex) throws DecoderException {
		return new SKey(Hex.decodeHex(hex.toCharArray()));
	}
}
