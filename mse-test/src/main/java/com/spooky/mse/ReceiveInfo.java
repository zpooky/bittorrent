package com.spooky.mse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import com.spooky.mse.io.Reader;
import com.spooky.mse.o.LocalPublicKey;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;
import com.spooky.mse.o.SharedSecret;
import com.spooky.mse.u.TransportCipher;

public class ReceiveInfo extends Base {

	private final SKey skey;
	private final LocalPublicKey publicKey;
	private final KeyAgreement keyAgreement;
	private final RemotePublicKey remotePublicKey;
	private final SecretKey secret;
	private final boolean outbound = false;

	public ReceiveInfo(SKey skey, LocalPublicKey publicKey, KeyAgreement keyAgreement, RemotePublicKey remotePublicKey, SecretKey secret) {
		this.skey = skey;
		this.publicKey = publicKey;
		this.keyAgreement = keyAgreement;
		this.remotePublicKey = remotePublicKey;
		this.secret = secret;
	}

	public MSEKeyPair complete(Reader reader) throws Exception {
		// B receives: HASH('req1', S), HASH('req2', SKEY)^HASH('req3', S),
		// ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
		// ENCRYPT(IA)

		if (!compareSecret(reader.requireExact(20))) {
			throw new Exception("Secret did not match");
		}

		SharedSecret sharedSecret = sharedSecret(reader);
		System.out.println(sharedSecret);
		SecretKeySpec a = a(secret, sharedSecret);
		SecretKeySpec b = b(secret, sharedSecret);

		TransportCipher read_cipher = new TransportCipher(Cipher.DECRYPT_MODE, outbound ? b : a);
		int padding = padding(reader, read_cipher);
		consume(reader, padding);
		ByteBuffer initialPayload = t(reader, read_cipher);

		consume(reader, initialPayload, read_cipher);

		TransportCipher writeCipher = new TransportCipher(Cipher.ENCRYPT_MODE, outbound ? a : b);
		return new MSEKeyPair(read_cipher, writeCipher);
	}

	private void consume(Reader reader, ByteBuffer initialPayload, TransportCipher read_cipher) throws Exception {
		reader.require(initialPayload);
		ByteBuffer out = ByteBuffer.allocate(initialPayload.remaining()).order(ByteOrder.BIG_ENDIAN);
		read_cipher.update(initialPayload, out);
		System.out.println("out: '" + new String(out.array(), UTF8) + "'");
	}

	private boolean compareSecret(ByteBuffer readBuffer) throws NoSuchAlgorithmException {
		MessageDigest hasher = MessageDigest.getInstance("SHA1");
		hasher.update(REQ1_IV);
		hasher.update(secret.raw);
		byte[] digest = hasher.digest();
		System.out.println("my: " + Hex.encodeHexString(digest) + "|other:" + Hex.encodeHexString(readBuffer.duplicate().array()));
		return compare20(readBuffer, digest);
	}

	private SharedSecret sharedSecret(Reader reader) throws Exception {
		final byte[] decode = new byte[20];
		reader.require(decode);

		MessageDigest hasher = MessageDigest.getInstance("SHA1");
		hasher.update(REQ3_IV);
		hasher.update(secret.raw);
		byte[] sha1 = hasher.digest();

		for (int i = 0; i < decode.length; i++) {

			decode[i] ^= sha1[i];
		}

		return new SharedSecret(decode);
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

	private int padding(Reader reader, TransportCipher read_cipher) throws Exception {
		// find SKEY using HASH('req2', SKEY)^HASH('req3', S) ,
		// ENCRYPT(VC, crypto_provide, len(PadC),
		final int INT = VC.length + 4 + 2;
		byte[] crypted = new byte[INT];
		reader.require(crypted);

		byte[] plain = read_cipher.update(crypted);

		System.out.println("crypted:" + Arrays.toString(crypted) + "|plain:" + Arrays.toString(plain));
		byte other_supported_protocols = plain[VC.length + 3];

		byte remoteSelectedProtocol;

		if ((other_supported_protocols & CRYPTO_RC4) != 0) {

			remoteSelectedProtocol = CRYPTO_RC4;

		} else {

			throw new IOException("No crypto protocol in common remote= " + other_supported_protocols);

		}

		int padding = ((plain[VC.length + 4] & 0xff) << 8) + (plain[VC.length + 5] & 0xff);

		if (padding > PADDING_MAX) {

			throw (new IOException("Invalid padding '" + padding + "'"));
		}

		return padding;
	}

	private static void consume(Reader reader, int padding) throws Exception {
		reader.requireExact(padding);
	}

	private ByteBuffer t(Reader reader, TransportCipher read_cipher) throws Exception {
		// ENCRYPT( len(IA)), { ENCRYPT(IA) }
		byte[] data = new byte[2];
		reader.require(data);

		data = read_cipher.update(data);

		int ia_len = 0xffff & (((data[data.length - 2] & 0xff) << 8) + (data[data.length - 1] & 0xff));

		if (ia_len > 65535) {

			throw (new IOException("Invalid IA length '" + ia_len + "'"));
		}

		if (ia_len > 0) {

			return ByteBuffer.allocate(ia_len).order(ByteOrder.BIG_ENDIAN);

		} else {
			//
			// read_buffer = null;
			//
			// continue;
			throw new RuntimeException(".");
		}

	}
}
