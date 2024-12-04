package org.hetils.jgl17;

import org.jetbrains.annotations.NotNull;

public class General {
    public static final String digits = "0123456789abcdef";
    public static @NotNull String toHex(Object o, int size) {
        o = o != null ? o.toString() : null;
        if (o instanceof String s) {
            int m = 0;
            for (int i = 0; i < s.length(); i++) m += s.charAt(i);
            return IntToHex(m, size);
        } else {
            return "0".repeat(size);
        }
    }

    public static @NotNull String IntToHex(int i, int size) {
        StringBuilder hex = new StringBuilder();
        while (i > 0) {
            int digit = i % 16;
            hex.insert(0, digits.charAt(digit));
            i = i / 16;
        }
        return "0".repeat(Math.max(0, size - hex.length())) + hex;
    }
}
