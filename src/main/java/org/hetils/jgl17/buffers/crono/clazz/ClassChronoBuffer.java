package org.hetils.jgl17.buffers.crono.clazz;

import org.hetils.jgl17.buffers.crono.ClassByteConverter;

import java.util.ArrayList;
import java.util.List;

public class ClassChronoBuffer<T> {

    public final int size;
    public final int data_size;
    protected final long[] timestamps;
    ArrayList<T> cache;
    public final ClassByteConverter<T> conv;
    public ClassChronoBuffer(int data_size, int cache_size, ClassByteConverter<T> conv) {
        this.conv = conv;
        this.size = cache_size;
        this.data_size = data_size+8;
        this.timestamps = new long[cache_size];
        this.cache = new ArrayList<>(cache_size);
        for (int i = 0; i < cache_size; i++)
            cache.add(null);
    }

    protected int index = 0;


    public void add(T t) {
        timestamps[index] = System.currentTimeMillis();
        cache.set(index++, t);
        if (index >= size)
            index = 0;
    }


    public List<T> readBack(int amount) {
        List<T> a = new ArrayList<>();
        int i = index;
        amount = amount > size ? size : amount;
        int added = 0;
        while (added < amount) {
            if (i >= size)
                i = 0;
            if (cache.get(i) != null)
                a.add(cache.get(i));
            i++;
            added++;
        }
        return a;
    }

    public ArrayList<TMoment<T>> getTMoments(int amount) {
        ArrayList<TMoment<T>> a = new ArrayList<>();
        int i = index;
        amount = amount > size ? size : amount;
        int added = 0;
        while (added < amount) {
            if (i >= size)
                i = 0;
            if (cache.get(i) != null)
                a.add(new TMoment<>(this.timestamps[i], cache.get(i)));
            i++;
            added++;
        }
        return a;
    }

    public ArrayList<TMoment<T>> getTMoments() {
        ArrayList<TMoment<T>> a = new ArrayList<>();
        int i = index;
        for (T t : cache)
            if (t != null)
                a.add(new TMoment<>(this.timestamps[i++], t));
        return a;
    }

}
