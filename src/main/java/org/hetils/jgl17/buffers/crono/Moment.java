package org.hetils.jgl17.buffers.crono;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Moment {

    public final long timestamp;
    public final String readable_time;
    public final ByteBuffer data;
    Moment(@NotNull ByteBuffer bb) {
        this.timestamp = bb.getLong();
        this.readable_time = Instant.ofEpochMilli(this.timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS"));
        this.data = bb;
    }

}
