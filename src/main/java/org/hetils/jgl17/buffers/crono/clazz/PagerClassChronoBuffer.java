package org.hetils.jgl17.buffers.crono.clazz;

import org.hetils.jgl17.buffers.crono.ClassByteConverter;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PagerClassChronoBuffer<T> extends PagedClassChronoBuffer<T> {

    public final ClassByteConverter<T> conv;
    int file_size;
    Path dir;
    private int page_file_c = 0;
    private long cfs = 0;
    public PagerClassChronoBuffer(Path dir_path,int data_size, int cache_size, int file_size, ClassByteConverter<T> conv) {
        super(null, data_size, cache_size, conv);
        this.file_size = file_size;
        try {
            Files.createDirectories(dir_path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.dir = dir_path;
        this.conv = conv;
        try (Stream<Path> paths = Files.list(dir)) {
            this.page_file_c = (this.page_file_c = (int) paths.filter(Files::isRegularFile).count()-1) < 0 ? 0 : this.page_file_c;
            this.cache_path = dir.resolve("part-" + page_file_c + ".dat");
            this.cfs = (this.cfs = Files.size(this.cache_path) / data_size) < 0 ? 0 : this.cfs;
        } catch (NoSuchFileException e) {
            this.cache_path = dir.resolve("part-0.dat");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void dump() {
        if (cache_path != null) {
            int i = 0;
            while (i < cache.size()) {
                try (DataOutputStream d = newStream(this.cache_path)) {
                    while (i < cache.size()) {
                        cfs++;
                        if (cfs > file_size) {
                            this.cache_path = dir.resolve("part-" + page_file_c++ + ".dat");
                            cfs = 0;
                            d.close();
                            break;
                        }
                        T t = cache.get(i);
                        if (t != null) {
                            d.writeLong(this.timestamps[i]);
                            d.write(conv.pack(t).array());
                        }
                        i++;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public ArrayList<TMoment<T>> getTMoments(int amount) {
        ArrayList<TMoment<T>> tl = super.getTMoments(amount);
        if (tl.size() < amount) {
            ArrayList<TMoment<T>> tlt = new ArrayList<>();
            int file = page_file_c;
            int data = tl.size();
            while (data < amount && file >= 0) {
                try (BufferedInputStream d = new BufferedInputStream(Files.newInputStream(dir.resolve("part-" + file-- + ".dat")))) {
                    byte[] bytes = d.readAllBytes();
                    int i = bytes.length;
                    while (i - data_size > 0 && data++ < amount) {
                        tlt.add(new TMoment<>(ByteBuffer.wrap(Arrays.copyOfRange(bytes, i - data_size, i)), conv));
                        i-=data_size;
                    }
                } catch (NoSuchFileException fe){
                    return tl;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            tlt.addAll(tl);
            return tlt;
        }
        return tl;
    }

    @Override
    public ArrayList<TMoment<T>> getTMoments() {
        ArrayList<TMoment<T>> tl = new ArrayList<>(page_file_c*file_size);
        int file = page_file_c;
        while (file >= 0) {
            try (BufferedInputStream d = new BufferedInputStream(Files.newInputStream(dir.resolve("part-" + file-- + ".dat")))) {
                byte[] bytes = d.readAllBytes();
                int i = bytes.length;
                while (i - data_size > 0) {
                    tl.add(new TMoment<>(ByteBuffer.wrap(Arrays.copyOfRange(bytes, i - data_size, i)), conv));
                    i-=data_size;
                }
            } catch (NoSuchFileException fe){
                return tl;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        tl.addAll(super.getTMoments());
        return tl;
    }

}
