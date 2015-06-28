package com.spooky.mse.io;

import java.nio.ByteBuffer;

public interface Writer {
	void write(ByteBuffer bb) throws Exception;
}
