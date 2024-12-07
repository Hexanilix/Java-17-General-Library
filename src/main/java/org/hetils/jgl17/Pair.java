package org.hetils.jgl17;

public class Pair<P1, P2> {
    protected P1 key;
    protected P2 value;
    public Pair(P1 obj1, P2 obj2) {
        this.key = obj1;
        this.value = obj2;
    }

    public Pair() {
        this.key = null;
        this.value = null;
    }

    public P1 key() {
        return key;
    }

    public P2 value() {
        return value;
    }

    public void setKey(P1 k) {
        this.key = k;
    }
    public void setValue(P2 v) {
        this.value = v;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}