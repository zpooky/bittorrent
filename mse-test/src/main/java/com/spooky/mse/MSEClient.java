package com.spooky.mse;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;

import com.spooky.mse.io.Reader;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.o.SKey;
import com.spooky.mse.outbound.Outbound;

public class MSEClient extends ExchangeBase {
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalStateException, InvalidAlgorithmParameterException, DecoderException, Exception {
		SocketAddress local = new InetSocketAddress("localhost", 25555);
		try (SocketChannel channel = SocketChannel.open(local)) {
			Reader r = r(channel);
			Writer w = w(channel);
			MSEKeyPair complete = new Outbound(SKey.fromHex("597f6a218a58b0fe7880ba12466ccd89ca6c778f")).sendPublicKey(w).receivePublicKey(r).complete(w);
		}
	}
}
