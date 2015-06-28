package com.spooky.mse.outbound;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.spooky.mse.Base;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.LocalPublicKey;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.o.RemotePublicKey;
import com.spooky.mse.o.SKey;
import com.spooky.mse.o.SecretKey;
import com.spooky.mse.o.SharedSecret;
import com.spooky.mse.u.TransportCipher;

public class SendInfo extends Base {

	private final SKey skey;
	private final LocalPublicKey localPublicKey;
	private final RemotePublicKey remotePublicKey;
	private final SecretKey secretKey;
	private final boolean outbound = true;

	public SendInfo(SKey skey, LocalPublicKey publicKey, RemotePublicKey remotePublicKey, SecretKey secretKey) {
		this.skey = skey;
		this.localPublicKey = publicKey;
		this.remotePublicKey = remotePublicKey;
		this.secretKey = secretKey;
	}

	public MSEKeyPair complete(Writer writer) throws Exception {

		// A->B: HASH('req1', S), HASH('req2', SKEY)^HASH('req3',
		// S), ENCRYPT(VC, crypto_provide, len(PadC), PadC,
		// len(IA)), ENCRYPT(IA)

		ByteBuffer initial_data_out = ByteBuffer.allocate(1024);
		initial_data_out.put("this is the inital data".getBytes(Charset.forName("UTF8")));
		initial_data_out.flip();
		int initial_data_out_len = initial_data_out == null ? 0 : initial_data_out.remaining();

		// padding_a here is half of the padding from before

		int pad_max = PADDING_MAX;

		byte[] padding_c = getZeroPadding(pad_max);

		ByteBuffer write_buffer = ByteBuffer.allocate(20 + 20 + (VC.length + 4 + 2 + padding_c.length + 2) + initial_data_out_len);

		byte[] secret_bytes = secretKey.raw;

		// HASH('req1', S)
		{
			MessageDigest hasher = MessageDigest.getInstance("SHA1");

			hasher.update(REQ1_IV);

			hasher.update(secret_bytes);

			byte[] sha1 = hasher.digest();

			write_buffer.put(sha1);
		}
		// HASH('req2', SKEY)^HASH('req3', S)
		{
			MessageDigest hasher = MessageDigest.getInstance("SHA1");

			hasher.update(REQ2_IV);
			hasher.update(skey.raw);

			byte[] sha1_1 = hasher.digest();

			hasher = MessageDigest.getInstance("SHA1");

			hasher.update(REQ3_IV);
			hasher.update(secret_bytes);

			byte[] sha1_2 = hasher.digest();

			for (int i = 0; i < sha1_1.length; i++) {

				sha1_1[i] ^= sha1_2[i];
			}

			write_buffer.put(sha1_1);
		}

		// SharedSecret sharedSecret = new SharedSecret(skey.raw);
		SharedSecret sharedSecret = SharedSecret.fromHex("597f6a218a58b0fe7880ba12466ccd89ca6c778f");
		System.out.println(sharedSecret);
		SecretKeySpec a = a(secretKey, sharedSecret);
		SecretKeySpec b = b(secretKey, sharedSecret);

		TransportCipher write_cipher = new TransportCipher(Cipher.ENCRYPT_MODE, outbound ? a : b);
		TransportCipher read_cipher = new TransportCipher(Cipher.DECRYPT_MODE, outbound ? b : a);

		// ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)
		{
			write_buffer.put(write_cipher.update(VC));

			write_buffer.put(write_cipher.update(new byte[] { 0, 0, 0, CRYPTO_RC4 }));

			write_buffer.put(write_cipher.update(new byte[] { (byte) (padding_c.length >> 8), (byte) padding_c.length }));

			write_buffer.put(write_cipher.update(padding_c));

			write_buffer.put(write_cipher.update(new byte[] { (byte) (initial_data_out_len >> 8), (byte) initial_data_out_len }));

			if (initial_data_out_len > 0) {

				int save_pos = initial_data_out.position();

				write_cipher.update(initial_data_out, write_buffer);

				// reset in case buffer needs to be used again by
				// caller

				initial_data_out.position(save_pos);

				initial_data_out = null;
			}
		}
		write_buffer.flip();

		writer.write(write_buffer);

		return new MSEKeyPair(read_cipher, write_cipher);
	}

	private void sasd() {
		//
		// // B->A: ENCRYPT(VC, crypto_select, len(padD), padD, //
		// // len(IB)), ENCRYPT(IB)
		//
		// int pad_max = PADDING_MAX;
		//
		// byte[] padding_b = getRandomPadding(pad_max / 2); // half
		// // padding
		// // b
		// // sent
		// // here
		//
		// byte[] padding_d = getZeroPadding(pad_max);
		//
		// ByteBuffer write_buffer = ByteBuffer.allocate(padding_b.length +
		// VC.length + 4 + 2 + padding_d.length); // +
		// // 2
		// // +
		// // initial_data_out.length
		// // );
		//
		// write_buffer.put(padding_b);
		//
		// write_buffer.put(write_cipher.update(VC));
		//
		// write_buffer.put(write_cipher.update(new byte[] { 0, 0, 0, CRYPTO_RC4
		// }));
		//
		// write_buffer.put(write_cipher.update(new byte[] { (byte)
		// (padding_d.length >> 8), (byte) padding_d.length }));
		//
		// write_buffer.put(write_cipher.update(padding_d));
		//
		// // write_buffer.put( write_cipher.update( new byte[]{
		// // (byte)(initial_data_out.length>>8),(byte)initial_data_out.length
		// // }));
		//
		// // write_buffer.put( write_cipher.update(
		// // initial_data_out ));
		//
		// write_buffer.flip();
		//
		// if (delay_outbound_4) {
		//
		// if (transport.delayWrite(write_buffer)) {
		//
		// write_buffer = null;
		//
		// handshakeComplete();
		//
		// } else {
		//
		// delay_outbound_4 = false;
		// }
		// }
		//
		// if (!delay_outbound_4) {
		//
		// write(write_buffer);
		//
		// if (!write_buffer.hasRemaining()) {
		//
		// write_buffer = null;
		//
		// handshakeComplete();
		// }
		// }
	}
}
