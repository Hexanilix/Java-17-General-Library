package org.hetils.jgl17.buffers.crono;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.hetils.jgl17.buffers.crono.ChronoTest.readFromFile;

public class PagedChronoBuffer extends ChronoBuffer {

    public static @Nullable DataOutputStream newDBGFStream(@NotNull Path path) {
        try {
            Path par = path.getParent();
            if (par != null)
                Files.createDirectories(par);
            return new DataOutputStream(
                    new BufferedOutputStream(
                            (
                                    Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
                            )
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    public Path cache_path;
    public final int data_size;
    public PagedChronoBuffer(Path cache_path, int cache_size, int data_size) {
        super(cache_size);
        this.data_size = data_size + 8;
        this.cache_path = cache_path;
    }

    @Override
    public void add(ByteBuffer t) {
        if (index >= size)
            dump();
        super.add(t);
    }

    public void dump() {
        if (cache_path != null) try (DataOutputStream d = newDBGFStream(cache_path)) {
            for (int i = 0; i < size; i++) {
                d.writeLong(this.timestamps[i]);
                d.write(this.cache[i].array());
            }
        } catch (Exception e) {
        }
    }

    @Override
    public @NotNull List<ByteBuffer> readBackBytes(int amount) {
        List<ByteBuffer> a = new ArrayList<>(amount);
        if (amount == size)
            for (int j = 0; j < size; j++) {
                ByteBuffer bb = ByteBuffer.allocate(data_size);
                bb.putLong(this.timestamps[j]);
                bb.put(this.cache[j]);
                a.add(bb.position(0));
            }
        else {
            int i = 0;
            if (amount < size) {
                for (ByteBuffer t : this.cache) {
                    a.add(t);
                    i++;
                    if (i == amount)
                        break;
                }
            } else {
                a = readFromFile(cache_path, data_size, amount - index);
                for (int j = 0; j < size; j++) {
                    ByteBuffer bb = ByteBuffer.allocate(data_size);
                    bb.putLong(this.timestamps[j]);
                    bb.put(this.cache[j]);
                    a.add(bb.position(0));
                }
            }
        }
        return a;
    }

}
