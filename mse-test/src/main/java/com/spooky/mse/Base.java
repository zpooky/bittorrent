package com.spooky.mse;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;

import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;
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
	public static void main(String[] args) throws DecoderException {
		// MessageDigest.isEqual(digesta, digestb)

		String[] t = {
				//
				"3acf8a36c7564543cb981380d97124c59dd5518dfbca847bcea20f73ba03274432069a5e99c7e41ca3639178228341f6d3e2df3c37550341a2c2146cbe64bd07f2ccf1a8b342849c1665dab83ced0b69a4594d39886e32aa43e64e2858547387",//
				"e0a3b08d12c2c264ccd1471343467eb15d3cb52ec6dd182d623a9b85282b0f17e7e5d2690a0811e1a3623f2004fa7ca7aaa0fdaf4a54e389c12318b36cd6b862ef282c6a5a4738358896d5dca7a915c2d209b7b994851122a65a08fec8d26e69",//
				"721bdfb846ea7f7d167795a1ddbd7f3a8a50aab8ecb078fd7bf07ff9866feec5b79f491bda8cabf1eaecaa96c79a38f8826ce2e4a1403fb51e6c964aee7e750e9e6e60a78316f7000b17c809f2f923cbab3b65d0e2c7bf5925bc1030a703bfc4",//
				"84d1ce2c7488f83fc18084718dfdc275466f4d4b86c4ad2d9041065b851ca334a6c08073917ed07cc6ac3ff4c19e0a5539f240e73d5e929e889e25c6b6070ee1d87e0cc2d84a3e4648f3267f12dce60e15ef93a6bf4d42e0e2320858635a0532",//
				"6c53a149b37f34b919c99d0da36bc66fa4945e81035a219c495b5c8813acf43845e1224c2d1586c78ec0f3f6028f129c08ff046342cce6cb82f0737b57d06a1f3dbc60e87273fbafbd8756697d93b192debebbe757e8f45fc079dac239fe22c1",//
				"a34b86194b7bc59c0d1a4613927dbe3a9bd63c8c3880e3e38f2f54a09c7c0dc9c2e21c9b855c2b8d1e3c49848564fa81c6fe709d24017ae5017de2509ffcf2cd36531e6281be7ee8b047034c54cc889fee8ece762a956a579f9e4c62ebe6c2ac",//
				"fcae30ccb27e5fe677c629758aad4dd62ff6c4dde524c787c94b58d5b721e833c67d64e8da3e337dfa7eaf3b4856925157183ceff6e2603ebb89e007748d7c1f2c83a0ebd291e60758e38bd779ed45a2b4c16ba39822621fe54c66cd8a637df9",//
				"29794e64c635d39b09721c133a822e01bf7e2bc4299770ef626782c09e54c5f4f03b2eeb25b7714e99a5c07489a717b6b15d0c911b5be032f2154642d20e8be07080c44258f9917834e05228c4c4e79560a889a9d3db6365536569203bfd5c01",//
				"43e8ae8621ddf76c5aa1b7f6229b2c75ec9b7ed639db47d57b9e425c06243e714c2cfb3312840bb2b51fa30bfb938941956e7ef815a480d77436de0743492cc33a97d89df88fb6b7e760476b77de000f3ef5276f43ae1ceae02024fce76c6db4",//
				"86587b07be0ba90bf36e43802edf23bb3a237771f2401837e85e460c08b8f4c91789f37a7676c0971d8a15ccf15cc6d721eafe1134b77d071e5bfdfc5fd7f709192f6d08d3b84c462354076339d767739837cb5728ad0047492a8c8e35afa326",//
				"77e9a53088f8da025df9339c2f63e3230c6479c318cbc0d08d3e16e54c31fae6bf9a96986418457b3193e91df565b8d2fe0f2f2a147b98f52b5ceeedeb19faec737961d7b098cd216edb9ca417dc18d6a8f551ae82e048457d32d10e8bb20c2d",//
		};
		for (String s : t) {
			byte[] decoded = Hex.decodeHex(s.toCharArray());
			byte[] b = new byte[DH_SIZE_BYTES];
			int a = 0;
			for (int i = b.length - decoded.length; i < b.length; ++i) {
				b[i] = decoded[a++];
			}
			Assert.assertEquals(a, decoded.length);
			// System.out.println("try:" + Hex.encodeHexString(b));
			// System.out.println("    " + Hex.encodeHexString(bigIntegerToBytes(new BigInteger(s, 16), DH_SIZE_BYTES)));
			// System.out.println("----");
			// System.out.println(b.length);
			// System.out.println(bigIntegerToBytes(new BigInteger(s, 16), DH_SIZE_BYTES).length);
			System.out.println(a + "|" + MessageDigest.isEqual(b, bigIntegerToBytes(new BigInteger(s, 16), DH_SIZE_BYTES)));
		}
		System.out.println(bigIntegerToBytes(BigInteger.TEN, DH_SIZE_BYTES));
	}

	protected static byte[] getRandomPadding(int size) {
		byte[] padding = new byte[size];
		ThreadLocalRandom random = ThreadLocalRandom.current();
		random.nextBytes(padding);
		return padding;
	}

	protected static byte[] bigIntegerToBytes(BigInteger bi, int num_bytes) {
		String str = bi.toString(16);
		// System.out.println(str.length() + "|" + str);
		while (str.length() < num_bytes * 2) {
			str = "0" + str;
		}
		// System.out.println(num_bytes * 2 + "||" + str);
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

	protected static SecretKeySpec a(SecretKey secretBytes, SKey secret) throws NoSuchAlgorithmException {
		byte[] secret_bytes = secretBytes.raw;
		byte[] shared_secret = secret.raw;

		MessageDigest hasher = MessageDigest.getInstance("SHA1");
		hasher.update(KEYA_IV);
		hasher.update(secret_bytes);
		hasher.update(shared_secret);

		byte[] a_key = hasher.digest();

		SecretKeySpec secret_key_spec_a = new SecretKeySpec(a_key, RC4_STREAM_ALG);
		return secret_key_spec_a;
	}

	protected static SecretKeySpec b(SecretKey secretBytes, SKey secret) throws NoSuchAlgorithmException {
		byte[] secret_bytes = secretBytes.raw;
		byte[] shared_secret = secret.raw;
		MessageDigest hasher = MessageDigest.getInstance("SHA1");

		hasher.update(KEYB_IV);
		hasher.update(secret_bytes);
		hasher.update(shared_secret);

		byte[] b_key = hasher.digest();

		SecretKeySpec secret_key_spec_b = new SecretKeySpec(b_key, RC4_STREAM_ALG);
		return secret_key_spec_b;
	}
}
