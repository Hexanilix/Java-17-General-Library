package org.hetils.jgl17;

import org.jetbrains.annotations.NotNull;

public class StringUtil {
    public static String grammar(int n) {
        int ld = n % 100;
        if (ld >= 11 && ld <= 13) { return n + "th"; }

        return switch (n % 10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }

    public static @NotNull String readableEnum(Enum<?> e) {
        if (e == null) return "null";
        String[] spl = e.name().toLowerCase().replace(" ", "_").split("_");
        StringBuilder s = new StringBuilder().append((char) (spl[0].charAt(0) - 32)).append(spl[0], 1, spl[0].length());
        for (int i = 1; i < spl.length; i++) {
            String st = spl[i];
            s.append(' ').append(((char) (st.charAt(0) - 32))).append(st, 1, st.length());
        }
        return s.toString();
    }
}
