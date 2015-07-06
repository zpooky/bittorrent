package com.spooky.mse.inbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import com.spooky.mse.Base;
import com.spooky.mse.io.Reader;
import com.spooky.mse.o.LocalPublicKey;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;
import com.spooky.mse.o.SharedSecret;
import com.spooky.mse.u.TransportCipher;

//ProtocolDecoderPHE
public class ReceiveInfo extends Base {

	private final SKey skey;
	private final LocalPublicKey publicKey;
	private final RemotePublicKey remotePublicKey;
	private final SecretKey secret;
	private final boolean outbound = false;

	public ReceiveInfo(SKey skey, LocalPublicKey publicKey, RemotePublicKey remotePublicKey, SecretKey secret) {
		this.skey = skey;
		this.publicKey = publicKey;
		this.remotePublicKey = remotePublicKey;
		this.secret = secret;
	}

	public Confirm complete(Reader reader) throws Exception {
		// B receives: HASH('req1', S), HASH('req2', SKEY)^HASH('req3', S),
		// ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
		// ENCRYPT(IA)

		if (!compareSecret(reader.requireExact(20))) {
			throw new Exception("Secret did not match");
		}

		/* SharedSecret sharedSecret = */sharedSecret(reader);
		// SharedSecret sharedSecret = new SharedSecret(skey.raw);
		// SharedSecret sharedSecret = SharedSecret.fromHex("597f6a218a58b0fe7880ba12466ccd89ca6c778f");
		// System.out.println(sharedSecret);
		SecretKeySpec a = a(secret, skey);
		SecretKeySpec b = b(secret, skey);

		TransportCipher readCipher = new TransportCipher(Cipher.DECRYPT_MODE, outbound ? b : a);
		int padding = padding(reader, readCipher);
		readCipher.update(reader.requireExact(padding));
		int initialLength = t(reader, readCipher);

		consume(reader, initialLength, readCipher);

		TransportCipher writeCipher = new TransportCipher(Cipher.ENCRYPT_MODE, outbound ? a : b);
		return new Confirm(new MSEKeyPair(readCipher, writeCipher));
	}

	private void consume(Reader reader, int initialLength, TransportCipher readCipher) throws Exception {
		if (initialLength > 0) {
			ByteBuffer initialPayload = reader.read();
			System.out.println("read: " + initialPayload.remaining() + "|expected: " + initialLength);
			ByteBuffer out = ByteBuffer.allocate(initialPayload.remaining()).order(ByteOrder.BIG_ENDIAN);
			readCipher.update(initialPayload, out);
			System.out.println("out: '" + new String(out.array(), UTF8) + "'");
		}
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

	private int padding(Reader reader, TransportCipher readCipher) throws Exception {
		// find SKEY using HASH('req2', SKEY)^HASH('req3', S) ,
		// ENCRYPT(VC, crypto_provide, len(PadC),
		final int INT = VC.length + 4 + 2;
		byte[] crypted = new byte[INT];
		reader.require(crypted);

		byte[] plain = readCipher.update(crypted);

		System.out.println("crypted:" + Arrays.toString(crypted) + "|plain:" + Arrays.toString(plain));
		byte other_supported_protocols = plain[VC.length + 3];

		if ((other_supported_protocols & CRYPTO_RC4) != 0) {

		} else {

			throw new RuntimeException("Not rc4 " + other_supported_protocols);

		}

		int padding = ((plain[VC.length + 4] & 0xff) << 8) + (plain[VC.length + 5] & 0xff);
		System.out.println("padding: " + padding);
		if (padding < 0) {
			throw new RuntimeException("Invlaid padding: " + padding);
		} else if (padding > PADDING_MAX) {

			throw (new IOException("Invalid padding '" + padding + "'"));
		}

		return padding;
	}

	private int t(Reader reader, TransportCipher readCipher) throws Exception {
		// ENCRYPT( len(IA)), { ENCRYPT(IA) }

		ByteBuffer data = readCipher.update(reader.requireExact(2));
		int ia_len = data.getShort() & 0xFFFF;

		System.out.println(Arrays.toString(data.array()) + "|data: " + ia_len);

		if (ia_len > 65535 || ia_len < 0) {

			throw new RuntimeException("Invalid IA length '" + ia_len + "'");
		}

		if (ia_len >= 0) {

			return ia_len;

		} else {
			//
			// read_buffer = null;
			//
			// continue;
			throw new RuntimeException("." + ia_len);
		}

	}
}
