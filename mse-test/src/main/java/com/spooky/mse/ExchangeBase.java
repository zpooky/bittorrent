package com.spooky.mse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import com.spooky.mse.io.Reader;
import com.spooky.mse.io.Writer;

public class ExchangeBase {
	protected static final int _24444 = 24444;

	protected static Writer w(final SocketChannel s) {
		return new Writer() {

			@Override
			public void write(ByteBuffer bb) throws IOException {
				while (bb.hasRemaining()) {
					s.write(bb);
				}

			}
		};
	}

	protected static Reader r(final SocketChannel s) {
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
				s.configureBlocking(true);
				while (bb.position() < amount) {
					int read = s.read(bb);
					if (read == -1) {
						throw new Exception("-1");
					}
					System.out.println(bb);

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

			@Override
			public ByteBuffer read() throws Exception {
				ByteBuffer bb = ByteBuffer.allocate((50 * 1024)).order(ByteOrder.BIG_ENDIAN);
				int read = 0;
				do {
					read = s.read(bb);
					if (read == -1) {
						throw new RuntimeException("-1");
					}
				} while (read == 0);
				bb.flip();
				return bb;
			}

		};
	}
}
