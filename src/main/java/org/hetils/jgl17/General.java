package org.hetils.jgl17;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class General {
    public static <T> T or(T t1, T t2) { return t1 != null ? t1 : t2; }

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

    public static class Stopwatch {
        private long time = System.nanoTime();
        private boolean running = false;
        public Stopwatch() {}
        public void start() {
            this.running = true;
            this.time = System.nanoTime();
        }
        public void stop() {
            running = false;
            this.time = System.nanoTime() - this.time;
        }
        public long getTime() {
            return running ? System.nanoTime() - time : time;
        }
    }

    public static <T> T getMostFrequentValue(Collection<T> c, T def) {
        if (c == null || c.isEmpty()) return def;

        Map<T, Integer> m = new HashMap<>();
        //Add an entry for each T or increase the count by one for that entry
        for (T item : c) m.put(item, m.getOrDefault(item, 0) + 1);

        T mf = null;
        int amnt = 0;

        //Cycle through all the entries to find the highest reoccurring T object
        for (Map.Entry<T, Integer> entry : m.entrySet())
            if (entry.getValue() > amnt) {
                amnt = entry.getValue();
                mf = entry.getKey();
            }

        //If no T has been found return the given default
        return mf != null ? mf : def;
    }
}
