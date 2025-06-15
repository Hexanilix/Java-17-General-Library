package org.hetils.jgl17.buffers.crono;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ChronoBuffer {

    final int size;
    long[] timestamps;
    ByteBuffer[] cache;
    public ChronoBuffer(int cache_size) {
        this.size = cache_size;
        this.cache = new ByteBuffer[cache_size];
        timestamps = new long[cache_size];
    }

    protected int index = 0;
    public void add(ByteBuffer t) {
        if (index >= size) {
            index = 0;
        }
        timestamps[index] = System.currentTimeMillis();
        cache[index++] = t;
    }

    public List<ByteBuffer> readBackBytes(int amount) {
        List<ByteBuffer> a = new ArrayList<>();
        int i = index;
        amount = amount > size ? size : amount;
        int added = 0;
        while (added < amount) {
            if (i >= size)
                i = 0;
            if (cache[i] != null)
                a.add(this.cache[i]);
            i++;
            added++;
        }
        return a;
    }

    public List<Moment> traceBack(int amount) {
        List<Moment> a = new ArrayList<>();
        for (ByteBuffer b : readBackBytes(amount))
            a.add(new Moment(b));
        return a;
    }

}
