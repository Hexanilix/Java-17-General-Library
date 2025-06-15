package org.hetils.jgl17.buffers.crono;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public interface ClassByteConverter<T> {

    @NotNull ByteBuffer pack(@NotNull T event);
    T unpack(@NotNull ByteBuffer buffer);

}
