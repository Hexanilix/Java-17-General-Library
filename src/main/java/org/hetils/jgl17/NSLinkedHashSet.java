package org.hetils.jgl17;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NSLinkedHashSet<T> extends LinkedHashSet<T> {

    public NSLinkedHashSet(@NotNull Collection<? extends T> c) { super(c.stream().filter(Objects::nonNull).collect(Collectors.toSet())); }
    public NSLinkedHashSet() { super(); }
    public NSLinkedHashSet(int initialCapacity) { super(initialCapacity); }
    public NSLinkedHashSet(int initialCapacity, float loadFactor) { super(initialCapacity, loadFactor); }


    @Override
    public boolean add(T t) { return t != null && super.add(t); }

    @Override
    public void forEach(Consumer<? super T> action) {
        try {
            super.remove(null);
        } catch (Exception ignore) {}
        super.forEach(action);
    }

    @Override
    public @NotNull Stream<T> stream() {
        return super.stream().filter(Objects::nonNull);
    }
}
