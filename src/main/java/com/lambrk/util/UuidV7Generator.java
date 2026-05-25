package com.lambrk.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * UUIDv7 generator combining Unix timestamp (ms) + random data.
 * Sortable, time-ordered, and ideal for database primary keys.
 */
public class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static UUID generate() {
        long timestamp = Instant.now().toEpochMilli();

        byte[] value = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(value);

        // 48-bit big-endian timestamp
        buffer.putShort((short) (timestamp >>> 32));
        buffer.putInt((int) timestamp);

        // 74 random bits
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);
        buffer.put(randomBytes);

        // Set version (7) in bits 48-51
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);

        // Set variant (10) in bits 64-65
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        long msb = ByteBuffer.wrap(value, 0, 8).getLong();
        long lsb = ByteBuffer.wrap(value, 8, 8).getLong();
        return new UUID(msb, lsb);
    }
}
