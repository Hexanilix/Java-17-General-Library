package org.hetils.jgl17;

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
}
