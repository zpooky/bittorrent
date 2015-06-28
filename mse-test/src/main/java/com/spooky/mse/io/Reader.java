package com.spooky.mse.io;

import java.nio.ByteBuffer;

public interface Reader {
	ByteBuffer requireAtleast(int amount) throws Exception;

	ByteBuffer requireExact(int amount) throws Exception;

	void require(byte[] bs) throws Exception;

	void require(ByteBuffer initialPayload) throws Exception;

	ByteBuffer read() throws Exception;
}
