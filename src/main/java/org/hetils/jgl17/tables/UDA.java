package org.hetils.jgl17.tables;

import org.hetils.jgl17.IDable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class UDA<T extends IDable> implements Set<T> {

    public final HashSet<T>[] d = new HashSet[] {
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>(),
                new HashSet<T>()
    };

    public UDA() {}

    public T get(@NotNull IDable ida) { return get(ida.getId()); }
    public T get(@NotNull UUID id) {
        int i = id.toString().charAt(0) - 87;
        for (T t : d[i < 0 ? i+39 : i])
            if (id.equals(t.getId()))
                return t;
        return null;
    }

    public boolean remove(@NotNull T e) {
        int i = e.getId().toString().charAt(0) - 87;
        return d[i < 0 ? i+39 : i].remove(e);
    }

    public boolean remove(@NotNull UUID id) {
        int i = id.toString().charAt(0) - 87;
        return d[i < 0 ? i+39 : i].removeIf(t -> id.equals(t.getId()));
    }

    public void clear() {
        for (HashSet<T> th : d)
            th.clear();
    }

    public void forEach(Consumer<? super T> action) {
        for (HashSet<T> th : d)
            th.forEach(action);
    }

    @Override
    public int size() {
        int size = 0;
        for (HashSet<T> th : d)
            size += th.size();
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (HashSet<T> th : d)
            if (!th.isEmpty())
                return false;
        return true;
    }

    @Override
    public boolean contains(Object o) { return o instanceof IDable t && get(t) != null; }

    public HashSet<T> getHashSet() {
        HashSet<T> t = new HashSet<>(size());
        for (HashSet<T> th : d)
            t.addAll(th);
        return t;
    }

    public ArrayList<T> getArrayList() {
        ArrayList<T> t = new ArrayList<>(size());
        for (HashSet<T> th : d)
            t.addAll(th);
        return t;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<>() {
            final ArrayList<T> arr = getArrayList();
            final int size = arr.size();
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                return index < size ? arr.get(index++) : null;
            }
        };
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        Object[] o = new Object[size()];
        int i = 0;
        for (HashSet<T> th : d)
            for (T t : th)
                o[i++] = t;
        return o;
    }

    @Override
    public @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        Object[] o = a;
        int i = 0;
        for (HashSet<T> th : d)
            for (T t : th)
                o[i++] = t;
        return a;
    }

    public boolean add(@NotNull T e) {
        int i = e.getId().toString().charAt(0) - 87;
        return d[i < 0 ? i+39 : i].add(e);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof IDable ida) {
            int i = ida.getId().toString().charAt(0) - 87;
            d[i < 0 ? i+39 : i].removeIf(t -> !t.equals(o));
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c)
            if (!(o instanceof IDable ida) || get(ida.getId()) == null)
                return false;
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        for (T t : c)
            if (!add(t))
                return false;
        return true;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        for (HashSet<T> th : d)
            for (Object o : c)
                th.removeIf(t -> !t.equals(o));
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        for (Object o : c)
            if (!remove(o))
                return false;
        return true;
    }

    @Override
    public String toString() {
        return "UDA{\n" +
                "\t0=" + d[0] + '\n' +
                "\t1=" + d[1] + '\n' +
                "\t2=" + d[2] + '\n' +
                "\t3=" + d[3] + '\n' +
                "\t4=" + d[4] + '\n' +
                "\t5=" + d[5] + '\n' +
                "\t6=" + d[6] + '\n' +
                "\t7=" + d[7] + '\n' +
                "\t8=" + d[8] + '\n' +
                "\t9=" + d[9] + '\n' +
                "\ta=" + d[10] + '\n' +
                "\tb=" + d[11] + '\n' +
                "\tc=" + d[12] + '\n' +
                "\td=" + d[13] + '\n' +
                "\te=" + d[14] + '\n' +
                "\tf=" + d[15] + '\n' +
                '}';
    }

//    public static void main(String[] args) {
//        UDA<IDable> d = new UDA<>();
//        HashSet<UUID> ids = new HashSet<>();
//        HashSet<IDable> dhs = new HashSet<>();
//        for (int i = 0; i < 100; i++) {
//            UUID id = UUID.randomUUID();
//            d.add(() -> id);
//            dhs.add(() -> id);
//            ids.add(id);
//        }
//
//        long time = 0;
//        long s;
//        for (UUID id : ids) {
//            s = System.nanoTime();
//            d.get(id);
////            for (IDable ida : dhs)
////                if (ida.getId() == id)
////                    break;
//            time += System.nanoTime() - s;
//        }
//
//        System.out.println(time + "ns");
//    }
}
