package com.github.anderskolsson.regserver.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Based on:
 * <a href="http://stackoverflow.com/a/29836273">this</a>.
 *
 */
public class UUIDUtils {
	public static UUID asUuid(byte[] bytes) {
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		long firstLong = bb.getLong();
		long secondLong = bb.getLong();
		return new UUID(firstLong, secondLong);
	}

	public static byte[] asBytes(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
}
