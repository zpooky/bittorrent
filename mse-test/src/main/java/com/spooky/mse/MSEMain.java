package com.spooky.mse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.spooky.mse.io.Reader;
import com.spooky.mse.io.Writer;
import com.spooky.mse.o.MSEKeyPair;
import com.spooky.mse.o.SKey;

public class MSEMain {
	public static void main(String[] args) throws Exception {
		SocketAddress local = new InetSocketAddress("localhost", 24444);
		try (ServerSocketChannel ss = ServerSocketChannel.open().bind(local)) {
			while (true) {
				try {
					try (SocketChannel s = ss.accept()) {
						Reader reader = r(s);
						Writer writer = w(s);
						MSEKeyPair keyPair = new Inbound(SKey.fromHex("597f6a218a58b0fe7880ba12466ccd89ca6c778f")).publicKey(reader).sendPublicKey(writer).complete(reader);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Thread.sleep(1);
				}
				System.out.println("---");
			}
		}
	}

	private static Writer w(final SocketChannel s) {
		return new Writer() {

			@Override
			public void write(ByteBuffer bb) throws IOException {
				while (bb.hasRemaining()) {
					s.write(bb);
				}

			}
		};
	}

	private static Reader r(final SocketChannel s) {
		return new Reader() {

			@Override
			public ByteBuffer requireExact(int amount) throws Exception {
				ByteBuffer out = ByteBuffer.allocate(amount).order(ByteOrder.BIG_ENDIAN);
				require(out);
				return out;
			}

			@Override
			public ByteBuffer requireAtleast(int amount) throws Exception {
				ByteBuffer bb = ByteBuffer.allocate(amount + (8 * 1024)).order(ByteOrder.BIG_ENDIAN);
				while (bb.position() <= amount) {
					int read = s.read(bb);
					if (read == -1) {
						throw new Exception("-1");
					}

				}
				bb.flip();
				return bb;
			}

			@Override
			public void require(ByteBuffer bb) throws Exception {
				while (bb.hasRemaining()) {
					int read = s.read(bb);
					if (read == -1) {
						throw new Exception("-1");
					}
				}
				bb.flip();
			}

			@Override
			public void require(byte[] bs) throws Exception {
				ByteBuffer out = ByteBuffer.wrap(bs).order(ByteOrder.BIG_ENDIAN);
				require(out);
			}
		};
	}
}
