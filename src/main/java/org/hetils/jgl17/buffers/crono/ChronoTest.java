package org.hetils.jgl17.buffers.crono;

import org.hetils.jgl17.buffers.crono.clazz.PagerClassChronoBuffer;
import org.hetils.jgl17.buffers.crono.clazz.TMoment;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ChronoTest {

    public static class TC {

        public long y;
        public int bs;
        public TC(long y, int bs) {
            this.y = y;
            this.bs = bs;
        }

        @Override
        public String toString() {
            return "TC{" +
                    "y=" + y +
                    ", bs=" + bs +
                    '}';
        }
    }

    public static @NotNull List<ByteBuffer> readFromFile(Path p, int data_size, int amount) {
        List<ByteBuffer> a = new ArrayList<>();
        int i = 0;
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(p, StandardOpenOption.READ))) {
            byte[] entry = new byte[data_size];
            while (bis.read(entry) == data_size && i++ < amount)
                a.add(ByteBuffer.wrap(Arrays.copyOf(entry, data_size)));
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
        return a;
    }

    public static @NotNull List<ByteBuffer> readFromFile(Path p, int data_size) {
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(p, StandardOpenOption.READ))) {
            byte[] data = bis.readAllBytes();
            List<ByteBuffer> a = new ArrayList<>(data.length/data_size);
            int i = 0;
            while (i - data_size > 0) {
                a.add(ByteBuffer.wrap(Arrays.copyOfRange(data, i-data_size, i)));
                i -= data_size;
            }
            return a;
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        ClassByteConverter<TC> converter = new ClassByteConverter<>() {

            @Override
            public @NotNull ByteBuffer pack(@NotNull TC tc) {
                ByteBuffer bb = ByteBuffer.allocate(12);
                bb.putLong(tc.y);
                return bb.putInt(tc.bs);
            }

            @Override
            public TC unpack(@NotNull ByteBuffer buffer) {
                return new TC(buffer.getLong(), buffer.getInt());
            }
        };

        PagerClassChronoBuffer<TC> cb = new PagerClassChronoBuffer<>(Path.of("test\\"), 12, 16, 8, converter);

//        Random r = new Random();
//        try {
//            for (int i = 0; i < 32; i++) {
//                cb.add(new TC(i, i));
////                Thread.sleep(r.nextInt(0, 25));
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        List<TMoment<TC>> l = cb.getTMoments(3);
        System.out.println(l.size());
        l.forEach(System.out::println);
    }

    public static String toBits(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }
}
