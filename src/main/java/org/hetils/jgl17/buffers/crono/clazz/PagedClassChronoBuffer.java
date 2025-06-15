package org.hetils.jgl17.buffers.crono.clazz;

import org.hetils.jgl17.buffers.crono.ClassByteConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.hetils.jgl17.buffers.crono.ChronoTest.readFromFile;

public class PagedClassChronoBuffer<T> extends ClassChronoBuffer<T> {

    public static @Nullable DataOutputStream newStream(@NotNull Path path) {
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

    protected Path cache_path;
    public final ClassByteConverter<T> conv;
    public PagedClassChronoBuffer(Path cache_path, int data_size, int cache_size, ClassByteConverter<T> conv) {
        super(data_size, cache_size, conv);
        this.cache_path = cache_path;
        this.conv = conv;
    }
    public PagedClassChronoBuffer(PagedClassChronoBuffer<T> b) { this(b.cache_path, b.size, b.data_size, b.conv); }

    public void add(T t) {
        super.add(t);
        if (index == 0)
            this.dump();
    }

    public void dump() {
        if (cache_path != null) try (DataOutputStream d = newStream(cache_path)) {
            for (int i = 0; i < cache.size(); i++) {
                d.writeLong(this.timestamps[i]);
                T t = cache.get(i);
                if (t != null) d.write(conv.pack(t).array());
            }
        } catch (Exception e) {
        }
    }

    public List<T> buildBack(int amount) {
        List<T> tl = new ArrayList<>(amount);
        if (amount < cache.size()) {
            int i = 0;
            for (T t : cache) {
                tl.add(t);
                if (i >= amount)
                    break;
            }
        }
        else if (amount == cache.size())
            return tl;
        else {
            for (ByteBuffer b : readFromFile(cache_path, data_size, amount - cache.size()))
                tl.add(conv.unpack(b.position(8)));
            tl.addAll(cache);
        }
        return tl;
    }

    @Override
    public ArrayList<TMoment<T>> getTMoments(int amount) {
        ArrayList<TMoment<T>> tl = new ArrayList<>(amount);
        int i = 0;
        for (T t : cache)
            if (t != null) {
                if (i >= amount)
                    break;
                tl.add(new TMoment<>(timestamps[i], t));
            }
        if (amount > cache.size()) {
            ArrayList<TMoment<T>> tlt = new ArrayList<>();
            for (ByteBuffer b : readFromFile(cache_path, data_size, amount - cache.size()))
                tlt.add(new TMoment<>(b, conv));
            tlt.addAll(tl);
            return tlt;
        }
        return tl;
    }

    @Override
    public ArrayList<TMoment<T>> getTMoments() {
        ArrayList<TMoment<T>> tl = new ArrayList<>(size*16);
        for (ByteBuffer b : readFromFile(cache_path, data_size))
            tl.add(new TMoment<>(b, conv));
        int i = 0;
        for (T t : cache)
            if (t != null)
                tl.add(new TMoment<>(timestamps[i++], t));
        return tl;
    }
}
