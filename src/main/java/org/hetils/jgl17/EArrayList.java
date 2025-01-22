package org.hetils.jgl17;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class EArrayList<E> extends java.util.ArrayList<E> {
    public EArrayList(int initialCapacity) { super(initialCapacity); }
    public EArrayList() { super(); }
    public EArrayList(@NotNull Collection<? extends E> c) { super(c); }


    public E getFirst() { return this.get(0); }
    public E getLast() { return this.get(this.size()-1); }

    public E removeFirst() { return this.remove(0); }
    public E removeLast() { return this.remove(this.size()-1); }

    public boolean remove(Object o, boolean from_end) {
        if (from_end) {
            for (int i = this.size() - 1; i >= 0; i--)
                if (this.get(i).equals(o)) {
                    this.remove(i);
                    return true;
                }
            return false;
        } else return remove(o);
    }
}
