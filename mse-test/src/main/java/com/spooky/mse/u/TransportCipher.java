/*
 * Created on 19-Jan-2006
 * Created by Paul Gardner
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.spooky.mse.u;

import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

public class TransportCipher {

	private final RC4Engine rc4_engine;

	public TransportCipher(int mode, SecretKeySpec key_spec) {

		rc4_engine = new RC4Engine();

		CipherParameters params = new KeyParameter(key_spec.getEncoded());

		rc4_engine.init(mode == Cipher.ENCRYPT_MODE, params);

		byte[] temp = new byte[1024];

		temp = update(temp);

	}

	public byte[] update(byte[] data) {
		return (update(data, 0, data.length));
	}

	public byte[] update(byte[] data, int offset, int length) {
		byte[] result;

		if (length == 0) {

			// watch out, cipher.update returns NULL with 0 length input

			result = new byte[0];

		} else {

			result = new byte[length];

			rc4_engine.processBytes(data, offset, length, result, 0);
		}

		return (result);
	}

	public ByteBuffer update(ByteBuffer source) {
		ByteBuffer target = ByteBuffer.allocate(source.remaining());
		update(source, target);
		target.flip();
		return target;
	}

	public void update(ByteBuffer source_buffer, ByteBuffer target_buffer) {
		try {
			// TODO: 1.5 supports update( ByteBuffer, ByteBuffer )

			byte[] source_bytes;
			int offset;
			int length = source_buffer.remaining();

			if (source_buffer.hasArray()) {

				source_bytes = source_buffer.array();

				offset = source_buffer.arrayOffset() + source_buffer.position();

			} else {

				source_bytes = new byte[length];

				offset = 0;

				source_buffer.get(source_bytes);
			}

			byte[] target_bytes = update(source_bytes, offset, length);

			source_buffer.position(source_buffer.limit());

			target_buffer.put(target_bytes);

		} catch (Throwable e) {

			throw new RuntimeException(e);
		}
	}

}
