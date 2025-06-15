package org.hetils.jgl17;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TypeUtil {

    @Contract(pure = true)
    public static long bytesToLong(byte @NotNull [] b) { return bytesToLong(b, 0); }
    @Contract(pure = true)
    public static long bytesToLong(byte @NotNull [] b, int offset) {
        return ((long)(b[offset] & 0xFF) << 56) |
                ((long)(b[offset + 1] & 0xFF) << 48) |
                ((long)(b[offset + 2] & 0xFF) << 40) |
                ((long)(b[offset + 3] & 0xFF) << 32) |
                ((long)(b[offset + 4] & 0xFF) << 24) |
                ((long)(b[offset + 5] & 0xFF) << 16) |
                ((long)(b[offset + 6] & 0xFF) << 8)  |
                ((long)(b[offset + 7] & 0xFF));
    }

    @Contract(pure = true)
    public static int bytesToInt(byte @NotNull [] b) { return bytesToInt(b, 0); }
    @Contract(pure = true)
    public static int bytesToInt(byte @NotNull [] b, int offset) {
        return  ((b[offset] & 0xFF) << 24) |
                ((b[offset + 1] & 0xFF) << 16) |
                ((b[offset + 2] & 0xFF) << 8)  |
                ((b[offset + 3] & 0xFF));
    }

}
