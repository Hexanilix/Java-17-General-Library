package org.hetils.jgl17.buffers.crono;

import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PagerChronoBuffer extends PagedChronoBuffer {

    private final Path dir;
    private int page_file_c = 0;
    private int cap;
    public PagerChronoBuffer(Path page_dir, int data_size, int cache_size, int dump_amount) {
        super(null, data_size, cache_size);
        this.dir = page_dir;
        this.cap = dump_amount;
        this.cache_path = dir.resolve("part-" + page_file_c++ + ".dat");
        try (Stream<Path> s = Files.list(dir)) {
            this.page_file_c = (int) s.filter(Files::isRegularFile).count();
        } catch (Exception e) {
            System.err.print("Couldn't retrieve parts of cache in " + page_dir);
        }
    }

    private int dumps = 0;
    @Override
    public void dump() {
        super.dump();
        if (dumps++ == cap) {
            this.cache_path = dir.resolve("part-" + page_file_c++ + ".dat");
            dumps = 0;
        }
    }

    protected List<ByteBuffer> readFromFile(int amount) {
        int fs = (amount / cap) + 1;
        List<ByteBuffer> a = new ArrayList<>();
        for (int i = page_file_c; i > fs; i--) {
            try (BufferedInputStream d = new BufferedInputStream(Files.newInputStream(dir.resolve("part-" + i + ".dat")))) {
                byte[] entry = new byte[data_size];
                while (d.read(entry) == data_size && i < amount) {
                    ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(entry, data_size));
                    a.add(bb);
                    i++;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return a;
    }
}
