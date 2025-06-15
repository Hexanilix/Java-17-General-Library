package org.hetils.jgl17.buffers.crono.clazz;

import org.hetils.jgl17.buffers.crono.ClassByteConverter;
import org.jetbrains.annotations.NotNull;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TMoment<T> {

    public final long timestamp;
    public final String readable_time;
    public final T t;
    TMoment(long time, T t) {
        this.timestamp = time;
        this.readable_time = Instant.ofEpochMilli(this.timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS"));
        this.t = t;
    }
    TMoment(@NotNull ByteBuffer bb, @NotNull ClassByteConverter<T> conv) {
        this(bb.getLong(), conv.unpack(bb));
    }

    @Override
    public String toString() {
        return "{" + readable_time + ": " + t + '}';
    }
}
