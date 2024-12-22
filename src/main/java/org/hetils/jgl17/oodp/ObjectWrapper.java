package org.hetils.jgl17.oodp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ObjectWrapper {

    public Object o;
    public ObjectWrapper() {}
    public ObjectWrapper(Object o) { this.o = o; }

    public String getString(String s) { return (String) o; }
    public String[] getStringArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        String[] arr = new String[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getString(String.valueOf(i));
        return arr;
    }
    public List<String> getStringList(String s) { return Arrays.asList(((String) o).split(",")); }

    public boolean getBoolean(String s) { return Boolean.parseBoolean((String) o); }
    public boolean[] getBooleanArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        boolean[] arr = new boolean[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getBoolean(String.valueOf(i));
        return arr;
    }

    public int getInt(String s) { return Integer.parseInt((String) o); }
    public int[] getIntArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        int[] arr = new int[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getInt(String.valueOf(i));
        return arr;
    }

    public long getLong(String s) { return Long.parseLong((String) o); }
    public long[] getLongArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        long[] arr = new long[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getLong(String.valueOf(i));
        return arr;
    }

    public float getFloat(String s) { return Float.parseFloat((String) o); }
    public float[] getFloatArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        float[] arr = new float[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getFloat(String.valueOf(i));
        return arr;
    }

    public double getDouble(String s) { return Double.parseDouble((String) o); }
    public double[] getDoubleArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        double[] arr = new double[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getDouble(String.valueOf(i));
        return arr;
    }

    public short getShort(String s) { return Short.parseShort((String) o); }
    public short[] getShortArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        short[] arr = new short[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getShort(String.valueOf(i));
        return arr;
    }

    public byte getByte(String s) { return Byte.parseByte((String) o); }
    public byte[] getByteArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        byte[] arr = new byte[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getByte(String.valueOf(i));
        return arr;
    }

    public char getChar(String s) {
        String value = (String) o;
        if (value.length() != 1)
            throw new IllegalArgumentException("String value is not a single character= " + value);
        return value.charAt(0);
    }

    public UUID getUUID(String s) { return UUID.fromString((String) o); }
    public UUID[] getUUIDArr(String s) {
        OODP.ObjectiveMap om = this.getObjectiveMap(s);
        UUID[] arr = new UUID[om.size()];
        for (int i = 0; i < om.size(); i++)
            arr[i] = om.getUUID(String.valueOf(i));
        return arr;
    }

    public java.math.BigInteger getBigInteger(String s) { return new java.math.BigInteger((String) o); }

    public java.math.BigDecimal getBigDecimal(String s) { return new java.math.BigDecimal((String) o); }

    public OODP.ObjectiveMap getObjectiveMap(String s) { return (OODP.ObjectiveMap) o; }
}
