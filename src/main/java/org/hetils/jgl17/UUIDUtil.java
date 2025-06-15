package org.hetils.jgl17;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDUtil {

    public static byte @NotNull [] uuidToBytes(@NotNull UUID uuid) {
        byte[] b = new byte[16];
        long l = uuid.getLeastSignificantBits();
        b[15] = (byte) (l & 0xFF);
        b[14] = (byte) (l >> 8 & 0xFF);
        b[13] = (byte) (l >> 16 & 0xFF);
        b[12] = (byte) (l >> 24 & 0xFF);
        b[11] = (byte) (l >> 32 & 0xFF);
        b[10] = (byte) (l >> 40 & 0xFF);
        b[9] = (byte) (l >> 48 & 0xFF);
        b[8] = (byte) (l >> 56 & 0xFF);
        long m = uuid.getMostSignificantBits();
        b[7] = (byte) (m & 0xFF);
        b[6] = (byte) (m >> 8 & 0xFF);
        b[5] = (byte) (m >> 16 & 0xFF);
        b[4] = (byte) (m >> 24 & 0xFF);
        b[3] = (byte) (m >> 32 & 0xFF);
        b[2] = (byte) (m >> 40 & 0xFF);
        b[1] = (byte) (m >> 48 & 0xFF);
        b[0] = (byte) (m >> 56 & 0xFF);
        return b;
    }

//    @Contract(value = "_ -> new", pure = true)
    public static @Nullable UUID uuidFromBytes(byte @NotNull [] b) { return uuidFromBytes(b, 0); }
    public static @Nullable UUID uuidFromBytes(byte @NotNull [] b, int offset) {
        if (b.length < 16) return null;
        long l = ((long) (b[offset + 15] & 0xFF)) |
                ((long) (b[offset + 14] & 0xFF) << 8) |
                ((long) (b[offset + 13] & 0xFF) << 16) |
                ((long) (b[offset + 12] & 0xFF) << 24) |
                ((long) (b[offset + 11] & 0xFF) << 32) |
                ((long) (b[offset + 10] & 0xFF) << 40) |
                ((long) (b[offset + 9] & 0xFF) << 48) |
                ((long) (b[offset + 8] & 0xFF) << 56);
        long m = ((long) (b[offset + 7] & 0xFF)) |
                ((long) (b[offset + 6] & 0xFF) << 8) |
                ((long) (b[offset + 5] & 0xFF) << 16) |
                ((long) (b[offset + 4] & 0xFF) << 24) |
                ((long) (b[offset + 3] & 0xFF) << 32) |
                ((long) (b[offset + 2] & 0xFF) << 40) |
                ((long) (b[offset + 1] & 0xFF) << 48) |
                ((long) (b[offset] & 0xFF) << 56);
        return new UUID(m, l);
    }


}
