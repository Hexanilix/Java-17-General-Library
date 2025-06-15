package org.hetils.jgl17;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    public static double matchPercentage(String s, String comp) {
        if (Objects.equals(s, comp) || "".equals(comp)) return 1d;
        if ("".equals(s)) return 0d;
        char[] sc = s.toCharArray();
        char[] cc = comp.toCharArray();
        int sl = sc.length;
        int cl = cc.length;
        int sim = 0;
        double mult = 1;
        int i = 0;
        int j = 0;
        double mult_sub = 1d/(sl*2);
        int ov = 0;
        while (sc[i] != cc[0] && ++i < sl);
        i = ov;
        if (sl == cl) {
            while (i < sl)
                if (sc[i] == cc[(i++) - ov]) sim++;
        } else {
            while (i < sl) {
                if (sc[i] == cc[j]) {
                    sim++;
                    if (++j >= cl) break;
                } else mult -= mult_sub;
                i++;
            }
        }
        return ((double) sim / sl)*mult;
    }
}
