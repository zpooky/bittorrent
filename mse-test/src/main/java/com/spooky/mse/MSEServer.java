package com.spooky.mse;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.spooky.mse.inbound.Inbound;
import com.spooky.mse.io.Reader;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.o.SKey;

public class MSEServer extends ExchangeBase {
	public static void main(String[] args) throws Exception {
		SocketAddress local = new InetSocketAddress("localhost", 25555);
		try (ServerSocketChannel ss = ServerSocketChannel.open().bind(local)) {
			while (true) {
				try {
					try (SocketChannel s = ss.accept()) {
						Reader reader = r(s);
						Writer writer = w(s);
						MSEKeyPair keyPair = //
						new Inbound(SKey.fromHex("597f6a218a58b0fe7880ba12466ccd89ca6c778f"))//
								.publicKey(reader)//
								.sendPublicKey(writer)//
								.complete(reader)//
								.confirm(writer)//
								.debug(reader);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Thread.sleep(1);
				}
				System.out.println("---");
			}
		}
	}

}
