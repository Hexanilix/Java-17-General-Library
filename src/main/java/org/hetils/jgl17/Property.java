package org.hetils.jgl17;

import java.util.*;

public class Property<T> {
    public static final List<Property> properties = new ArrayList<>();
    
    private final String prop;
    private final T defVal;
    private T value;
    
    public Property(String property, T value) {
        this.prop = property;
        this.defVal = value;
        this.value = value;
        properties.add(this);
    }

    public String p() {
        return prop;
    }

    public T d() {
        return defVal;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public T v() {
        return value;
    }

    public void setV(T value) {
        this.value = value;
    }
}
