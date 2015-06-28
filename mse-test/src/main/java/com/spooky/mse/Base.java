package com.spooky.mse;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.spec.SecretKeySpec;

import com.spooky.mse.o.SecretKey;
import com.spooky.mse.o.SharedSecret;
import com.spooky.mse.u.ByteFormatter;

public class Base {

	protected static final Charset UTF8 = Charset.forName("UTF8");
	protected static final byte[] KEYA_IV = "keyA".getBytes(UTF8);
	protected static final byte[] KEYB_IV = "keyB".getBytes(UTF8);
	protected static final byte[] REQ1_IV = "req1".getBytes(UTF8);
	protected static final byte[] REQ2_IV = "req2".getBytes(UTF8);
	protected static final byte[] REQ3_IV = "req3".getBytes(UTF8);
	protected static final byte[] VC = { 0, 0, 0, 0, 0, 0, 0, 0 };

	//
	protected static final String RC4_STREAM_ALG = "RC4";

	//
	protected static final String DH_P = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A63A36210000000000090563";
	protected static final String DH_G = "02";
	protected static final int DH_L = 160;

	protected static final int DH_SIZE_BYTES = DH_P.length() / 2;

	protected static final int MIN_INCOMING_INITIAL_PACKET_SIZE = DH_SIZE_BYTES;

	protected static final BigInteger DH_P_BI = new BigInteger(DH_P, 16);
	protected static final BigInteger DH_G_BI = new BigInteger(DH_G, 16);
	//
	protected static final byte CRYPTO_RC4 = 0x02;
	//
	protected static final int PADDING_MAX = 512;

	protected static final int PADDING_MAX_NORMAL = PADDING_MAX;

	//

	protected static byte[] getRandomPadding(int size) {
		byte[] padding = new byte[size];
		ThreadLocalRandom random = ThreadLocalRandom.current();
		random.nextBytes(padding);
		return padding;
	}

	protected static byte[] bigIntegerToBytes(BigInteger bi, int num_bytes) {
		String str = bi.toString(16);

		while (str.length() < num_bytes * 2) {
			str = "0" + str;
		}

		return (ByteFormatter.decodeString(str));
	}

	protected static BigInteger bytesToBigInteger(byte[] bytes, int offset, int len) {
		return (new BigInteger(ByteFormatter.encodeString(bytes, offset, len), 16));
	}

	protected static byte[] getZeroPadding(int max_len) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		byte[] bytes = new byte[random.nextInt(max_len)];

		return (bytes);
	}

	protected static boolean compare20(ByteBuffer readBuffer, byte[] digest) {
		for (int i = 0; i < 20; i++) {
			if (readBuffer.get() != digest[i]) {
				return false;
			}
		}
		return true;
	}

	protected static SecretKeySpec a(SecretKey secretBytes, SharedSecret sharedSecret) throws NoSuchAlgorithmException {
		byte[] secret_bytes = secretBytes.raw;
		byte[] shared_secret = sharedSecret.raw;

		MessageDigest hasher = MessageDigest.getInstance("SHA1");
		hasher.update(KEYA_IV);
		hasher.update(secret_bytes);
		hasher.update(shared_secret);

		byte[] a_key = hasher.digest();

		SecretKeySpec secret_key_spec_a = new SecretKeySpec(a_key, RC4_STREAM_ALG);
		return secret_key_spec_a;
	}

	protected static SecretKeySpec b(SecretKey secretBytes, SharedSecret sharedSecret) throws NoSuchAlgorithmException {
		byte[] secret_bytes = secretBytes.raw;
		byte[] shared_secret = sharedSecret.raw;
		MessageDigest hasher = MessageDigest.getInstance("SHA1");

		hasher.update(KEYB_IV);
		hasher.update(secret_bytes);
		hasher.update(shared_secret);

		byte[] b_key = hasher.digest();

		SecretKeySpec secret_key_spec_b = new SecretKeySpec(b_key, RC4_STREAM_ALG);
		return secret_key_spec_b;
	}
}
