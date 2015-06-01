package com.spooky;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

public class Rc4Test {
	private static final Charset ascii = Charset.forName("ASCII");

	public static void main(String[] args) {
		byte[] encrypt = encrypt("Plaintext");
		System.out.println(Hex.encodeHexString(encrypt));
		System.out.println(decrypt(encrypt));
	}

	private static byte[] encrypt(String plaintext) {
		byte[] raw = plaintext.getBytes(ascii);
		return encrypt(raw);
	}

	private static String decrypt(byte[] raw) {
		byte[] out = encrypt(raw);
		return new String(out, ascii);
	}

	public static byte[] encrypt(byte[] raw) {
		RC4Engine rc4Engine = new RC4Engine();
		KeyParameter keyParam = new KeyParameter("Key".getBytes(ascii));
		rc4Engine.init(true, keyParam);
		byte[] out = new byte[raw.length];
		rc4Engine.processBytes(raw, 0, raw.length, out, 0);
		return out;
	}
}
