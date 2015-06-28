package com.spooky.mse.o;

import org.apache.commons.codec.binary.Hex;

public abstract class ArrayWrapper {
	public final byte[] raw;

	public ArrayWrapper(byte[] raw) {
		this.raw = raw;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + Hex.encodeHexString(raw) + ")";
	}
}
