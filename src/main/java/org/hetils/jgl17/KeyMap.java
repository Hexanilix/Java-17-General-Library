package org.hetils.jgl17;

import org.hetils.jgl17.oodp.OODPExclude;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class KeyMap<T> implements Map<String, T> {
    private static final int DEF_SIZE = 16;
    private static final int DEF_RE_SIZE = 4;
    private static final int DEF_RE_THRESH = 4;

    private int size;
    private int lk = 0;
    private int restruct_threshhold;
    private int restruct_range;
    private String[] strs;
    private T[] vals;
    private int[] calls;

    public KeyMap() { this(DEF_SIZE, DEF_RE_SIZE, DEF_RE_THRESH); }
    public KeyMap(int s) { this(s, s/4, s/4); }
    public KeyMap(int s, int re_range, int re_thresh) {
        this.size = s;
        this.restruct_range = re_range;
        this.restruct_threshhold = re_thresh;
        set(s);
    }

    private void set(int size) {
        this.strs = new String[size];
        this.vals = (T[]) new Object[size];
        this.calls = new int[size];
    }

    public void resize(int size) {
        int ext = size - this.size;
        String[] cks = this.strs;
        T[] cv = this.vals;
        set(size);
        this.strs = Arrays.copyOfRange(cks, 0, size-1);
        this.vals = Arrays.copyOfRange(cv, 0, size-1);
    }

    public boolean has(String key) {
        for (String str : strs)
            if (str == key)
                return true;
        return false;
    }

    private void restructure() {
        int max = 0;
        for (int t = size-1; t >= Math.max(0, size- restruct_range); t--) {
            for (int i = 0; i < size; i++) {
                if (calls[max] < calls[i])
                    max = i;
            }
            {
                int p = calls[max];
                calls[max] = calls[t];
                calls[t] = p;
            }
            {
                String p = strs[max];
                strs[max] = strs[t];
                strs[t] = p;
            }
            {
                T p = vals[max];
                vals[max] = vals[t];
                vals[t] = p;
            }
        }
    }

    @OODPExclude
    private int call_cound = 0;
    public T get(String key) {
        for (int i = 0; i < strs.length-1; i++)
            if (strs[i] == key) {
                calls[i]++;
                if (i >= restruct_range && restruct_threshhold > 0) {
                    call_cound++;
                    if (call_cound == restruct_threshhold) restructure();
                    else call_cound = 0;
                }
                return vals[i];
            }
        return null;
    }

    @Override
    public T get(Object key) {
        return get(String.valueOf(key));
    }

    @Override
    public T put(String key, T v) {
        T ov = get(key);
        strs[lk] = key;
        vals[lk] = v;
        lk++;
        return ov;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public T remove(Object key) {
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends T> m) {

    }

    @Override
    public void clear() { set(size); }

    @Override
    public @NotNull Set<String> keySet() { return new HashSet<>(List.of(strs)); }

    @Override
    public @NotNull Collection<T> values() { return List.of(vals); }

    @Override
    public @NotNull Set<Entry<String, T>> entrySet() {
        Set<Entry<String, T>> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            int fi = i;
            set.add(new Entry<>() {
                @Override
                public String getKey() {
                    return strs[fi];
                }

                @Override
                public T getValue() {
                    return vals[fi];
                }

                @Override
                public T setValue(T value) {
                    return value;
                }
            });
        }
        return set;
    }
}
