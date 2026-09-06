package br.ufpr.sept.so2.shared.infrastructure;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Gerador de UUID v7 (timestamp + aleatório) para ordenação cronológica
 * e mitigação de enumeração, conforme a especificação do SO2.
 */
public final class Uuids {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Uuids() {
    }

    public static UUID v7() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);

        long timestampMs = System.currentTimeMillis();
        bytes[0] = (byte) ((timestampMs >>> 40) & 0xFF);
        bytes[1] = (byte) ((timestampMs >>> 32) & 0xFF);
        bytes[2] = (byte) ((timestampMs >>> 24) & 0xFF);
        bytes[3] = (byte) ((timestampMs >>> 16) & 0xFF);
        bytes[4] = (byte) ((timestampMs >>> 8) & 0xFF);
        bytes[5] = (byte) (timestampMs & 0xFF);

        bytes[6] = (byte) ((bytes[6] & 0x0F) | 0x70);
        bytes[8] = (byte) ((bytes[8] & 0x3F) | 0x80);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
