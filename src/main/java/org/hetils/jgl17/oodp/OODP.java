package org.hetils.jgl17.oodp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static org.hetils.jgl17.General.or;

//TODO create readable oodp strings (Object Oriented Data Preserving)
@SuppressWarnings("unchecked")
public class OODP {
    public static class Converter<I, T> {
        private Class<I> i;
        private Class<T> o;
        private final Function<I, T> f;
        public Converter(Class<I> in, Class<T> out, Function<I, T> func) {
            i = in;
            o = out;
            f = func;
        }
        public Class<I> getI() { return i; }
        public Class<T> getO() { return o; }
        public T create(I in) { return f.apply(in); }
    }

    //TODO add get...Or methods
    public class ObjectiveMap extends HashMap<String, String> {
        //omfg THANK GOD FOR CHATGPT without em I would not have figured of how tf component classes in arrays work,
        // although I had to figure out myself that if Class<?> is a List it doesn't retain its component
        // and you gotta pass a ParameterizedType instead said Class<?>. The more you know

        public ObjectiveMap() { super(); }
        public ObjectiveMap(Map<? extends String, ? extends String> m) { super(m); }
        public ObjectiveMap(String key, String value) { super(); this.put(key, value); }

        public boolean has(String key) { return this.containsKey(key); }

        public <I, T> T as(@NotNull Converter<I, T> c) { return c.f.apply(this.as(c.i)); }

        public <T> T as(Class<T> clazz) { return as(clazz, false); }
        public <T> T as(Class<T> clazz, boolean auto) { return auto ? omToClass(this, clazz) : parseMapTo(this, clazz); }

        public <K, V> HashMap<K, V> asHashMap(Class<K> keyc, Class<V> valc) {
            HashMap<K, V> map = new HashMap<>();
            for (String key : this.keySet()) {
                if (key.startsWith("|")) {
                    Class<?> c;
                    try {
                        c = Class.forName(key.substring(1).split("\\|")[0]);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    String k = key.split("\\|")[2];
                    map.put((K) parseTo(k.substring(0, k.length()-1), c), parseTo(this.getString(key), valc));
                } else {
                    map.put(parseTo(key.substring(0, key.length()-1), keyc), parseTo(this.getString(key), valc));
                }
            }
            return map;
        }

        /**
         * This function simply return the wrapper type for primitive classes
         *
         * @return wrapper type for primitive type {clazz}
         */
        @Contract(pure = true)
        private static @Nullable Class<?> getWrapperType(Class<?> clazz) {
            if (clazz == int.class) return Integer.class;
            if (clazz == double.class) return Double.class;
            if (clazz == float.class) return Float.class;
            if (clazz == long.class) return Long.class;
            if (clazz == short.class) return Short.class;
            if (clazz == byte.class) return Byte.class;
            if (clazz == char.class) return Character.class;
            if (clazz == boolean.class) return Boolean.class;
            return null;
        }

        public <T> T get(String key, Class<T> clazz, T def) {
            T ob = get(key, clazz);
            if (ob != null) return ob;
            else return def;
        }

        public <T> T get(String key, Class<T> clazz) {
            Object o = null;
            if (containsKey(key)) {
                if (isDefault(clazz)) {
                    if (clazz == String.class) o = getString(key);
                    else if (clazz == int.class || clazz == Integer.class) o = getInt(key);
                    else if (clazz == double.class || clazz == Double.class) o = getDouble(key);
                    else if (clazz == long.class || clazz == Long.class) o = getLong(key);
                    else if (clazz == boolean.class || clazz == Boolean.class) o = getBoolean(key);
                    else if (clazz == float.class || clazz == Float.class) o = getFloat(key);
                    else if (clazz == char.class || clazz == Character.class) o = getString(key).charAt(0);
                    else if (clazz == byte.class || clazz == Byte.class) o = getByte(key);
                    else if (clazz == short.class || clazz == Short.class) o = getShort(key);
                    else if (clazz == UUID.class) o = getUUID(key);
                    if (o != null && (clazz.equals(o.getClass()) || getWrapperType(clazz) == o.getClass())) return (T) o;
                    else throw new RuntimeException((o != null ? o.getClass().getName() : "null") + " is not an instance of " + clazz.getName());
                } else if (clazz.isEnum()) {
                    String enumName = getString(key);
                    try {
                        return (T) Enum.valueOf((Class<Enum>) clazz, enumName);
                    } catch (IllegalArgumentException e) { throw new RuntimeException("No enum constant " + clazz.getName() + "." + enumName, e); }
                } else if (clazz.isArray()) {
                    if (clazz == int[].class) return (T) getIntArr(key);
                    else if (clazz == double[].class) return (T) getIntArr(key);
                    else if (clazz == long[].class) return (T) getLongArr(key);
                    else if (clazz == boolean[].class) return (T) getBooleanArr(key);
                    else if (clazz == float[].class) return (T) getFloatArr(key);
                    else if (clazz == byte[].class) return (T) getByteArr(key);
                    else if (clazz == short[].class) return (T) getShortArr(key);
                    else if (clazz == UUID[].class) return (T) getUUIDArr(key);
                    else return (T) getArray(key, clazz.getComponentType());
                } else if (clazz == ObjectiveMap.class)
                    return (T) OODP.this.map(getRaw(key));
                else return parseTo(getRaw(key), clazz);
            }
            return null;
        }

        public <T> Object getObjectArray(String key, Class<T> clazz) {
            if (clazz == String.class) return getStringArr(key);
            else if (clazz == int.class || clazz == Integer.class) return getIntArr(key);
            else if (clazz == double.class || clazz == Double.class) return getDoubleArr(key);
            else if (clazz == long.class || clazz == Long.class) return getLongArr(key);
            else if (clazz == boolean.class || clazz == Boolean.class) return getBoolean(key);
            else if (clazz == float.class || clazz == Float.class) return getFloat(key);
            else if (clazz == char.class || clazz == Character.class) return getString(key);
            else if (clazz == byte.class || clazz == Byte.class) return getByteArr(key);
            else if (clazz == short.class || clazz == Short.class) return getShortArr(key);
            else if (clazz == UUID.class) return getUUIDArr(key);
            else return getArray(key, clazz);
        }

        public <T> T[] getArray(String key, Class<T> clazz) {
            if (isDefault(clazz)) throw new RuntimeException("Do not use 'getArray()' for primitives, instead use a dedicated function for the desired type ex. 'getIntArr()', 'get(<key>, int[].class)' or 'getObjectArray(<key>, int[].class)'");
            List<String> s = getCutArr(key);
            T[] arr = (T[]) Array.newInstance(clazz, s.size());
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), clazz);
            return arr;
        }

        public Object[] getArray(String key) {
            ObjectiveMap om = this.map(key);
            Object[] arr = new Object[om.size()];
            for (int i = 0; i < om.size(); i++)
                for (Class<?> c : DEFAULT_CLASSES)
                    try {
                        arr[i] = om.get(String.valueOf(i), c);
                        break;
                    } catch (ClassCastException ignored) {}
            return arr;
        }

        private @NotNull List<String> getCutArr(String key) {
            String raw = getRaw(key);
            return smartSplit(raw.substring(1, raw.length()-1), ',');
        }

        public String getRaw(String key) { return this.get(key); }

        public String getString(String s) {
            String rs = this.get(s);
            if (rs != null && rs.charAt(0) == '\"') rs = rs.substring(1, rs.length()-1);
            return rs;
        }
        public String[] getStringArr(String key) {
            List<String> s = getCutArr(key);
            String[] arr = new String[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = s.get(i);
            return arr;
        }

        public boolean getBoolean(String s) {
            try {
                return Boolean.parseBoolean(this.get(s));
            } catch (Exception e) {
                return false;
            }
        }
        public boolean[] getBooleanArr(String key) {
            List<String> s = getCutArr(key);
            boolean[] arr = new boolean[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), boolean.class);
            return arr;
        }

        public int getInt(String s) {
            try {
                return Integer.parseInt(this.get(s));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        public int[] getIntArr(String key) {
            List<String> s = getCutArr(key);
            int[] arr = new int[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), int.class);
            return arr;
        }

        public long getLong(String s) {
            try {
                return Long.parseLong(this.get(s));
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        public long[] getLongArr(String key) {
            List<String> s = getCutArr(key);
            long[] arr = new long[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), long.class);
            return arr;
        }

        public float getFloat(String s) {
            try {
                return Float.parseFloat(this.get(s));
            } catch (NumberFormatException e) {
                return 0f;
            }
        }
        public float[] getFloatArr(String key) {
            List<String> s = getCutArr(key);
            float[] arr = new float[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), float.class);
            return arr;
        }

        public double getDouble(String s) {
            try {
                return Double.parseDouble(this.get(s));
            } catch (NumberFormatException e) {
                return 0d;
            }
        }
        public double[] getDoubleArr(String key) {
            List<String> s = getCutArr(key);
            double[] arr = new double[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), double.class);
            return arr;
        }

        public short getShort(String s) {
            try {
                return Short.parseShort(this.get(s));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        public short[] getShortArr(String key) {
            List<String> s = getCutArr(key);
            short[] arr = new short[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), short.class);
            return arr;
        }

        public byte getByte(String s) {
            try {
                return Byte.parseByte(this.get(s));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        public byte[] getByteArr(String key) {
            List<String> s = getCutArr(key);
            byte[] arr = new byte[s.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = parseTo(s.get(i), byte.class);
            return arr;
        }

        public UUID getUUID(String key) {
            try {
                return UUID.fromString(this.get(key));
            } catch (Exception e) {
                return null;
            }
        }
        public UUID[] getUUIDArr(String key) {
            List<String> s = getCutArr(key);
            UUID[] arr = new UUID[s.size()];
            for (int i = 0; i < arr.length; i++)
                arr[i] = parseTo(s.get(i), UUID.class);
            return arr;
        }

        public ObjectiveMap map(String s) { return OODP.this.map(this.getRaw(s)); }

        private final Converter<String, ObjectiveMap> stolc = new Converter<>(String.class, ObjectiveMap.class, OODP.this::map);
        public List<ObjectiveMap> getObjectiveList(String key) { return getList(key, stolc); }

        //TODO fix
//        public <T> List<T> getList(String key, @NotNull ParameterizedType clazz) {
//            String raw = getRaw(key);
//            if (raw.startsWith("{")) raw = raw.substring(1, raw.length()-1);
//            Class<T> rt = (Class<T>) clazz.getActualTypeArguments()[0];
//            if (isTable(rt)) return (List<T>) smartSplit(raw, ',').stream().map(s -> stringToList(s, rt)).toList();
//            else return stringToList(raw, rt);
//        }

        public Set<?> getSet(String key, Type clazz) { return new HashSet<>(parseList(getRaw(key), (Class<?>) clazz)); }
        public <T> Set<T> getSet(String key, Class<T> clazz) { return new HashSet<>(parseList(getRaw(key), clazz)); }
        public <T> Set<T> getSet(String key, Class<T> out, Function<ObjectiveMap, T> c) { return new HashSet<>(getList(key, out, c)); }
        public <T> Set<T> getSet(String key, Converter<?, T> c) { return new HashSet<>(parseList(getRaw(key), c)); }
        public <I, T> Set<T> getSet(String key, Class<I> in, Class<T> out, Function<I, T> c) { return new HashSet<>(getList(key, in, out, c)); }

        public List<?> getList(String key, Type clazz) { return parseList(getRaw(key), (Class<?>) clazz); }
        public <T> List<T> getList(String key, Class<T> clazz) { return parseList(getRaw(key), clazz); }
        public <T> List<T> getList(String key, Class<T> out, Function<ObjectiveMap, T> c) { return parseList(getRaw(key), s -> c.apply(OODP.this.map(s))); }
        public <T> List<T> getList(String key, Converter<?, T> c) { return parseList(getRaw(key), c); }
        public <I, T> List<T> getList(String key, Class<I> in, Class<T> out, Function<I, T> c) { return parseList(getRaw(key), s -> c.apply(parseTo(s, in))); }

        public <I, T> List<T> parseList(String raw, Converter<I, T> c) { return parseList(raw, s -> c.f.apply(parseTo(s, c.i))); }
        public <T> List<T> parseList(String raw, Class<T> out) { return parseList(raw, s -> parseTo(s, out)); }
        public <T> List<T> parseList(String raw, Function<String, T> f) {
            if (raw == null) return new ArrayList<>();
            if (raw.startsWith("[") || raw.startsWith("{")) raw = raw.substring(1, raw.length()-1);
            return new ArrayList<>(smartSplit(raw, ',').stream().map(f).toList());
        }

        @Override
        public String toString() { return "{" + String.join(",", this.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList()) + "}"; }
        public byte[] getBytes() { return this.toString().getBytes(); }
    }

    private static final Class<?>[] DEFAULT_CLASSES = new Class<?>[]{
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            Boolean.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            UUID.class,
            String.class,
            char.class,
            Character.class,
    };
    public static boolean isDefault(@NotNull Class<?> clazz) {
        if (clazz.isPrimitive()) return true;
        for (Class<?> defaultClass : DEFAULT_CLASSES) if (clazz == defaultClass) return true;
        return false;
    }

    private static final Set<Class<?>> STRINGABLE_CLASSES = new HashSet<>();
    static {

    }
    public static boolean isStringable(Class<?> c) { return isDefault(c) || STRINGABLE_CLASSES.contains(c); }

    private static final Set<Class<?>> DOESNT_REQUIRES_PARAGRAPHS = new HashSet<>();
    static {
        DOESNT_REQUIRES_PARAGRAPHS.add(byte.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(short.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(int.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(long.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(float.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(double.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(UUID.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(Boolean.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(Byte.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(Short.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(Integer.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(Long.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(Float.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(Double.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(BigDecimal.class);
        DOESNT_REQUIRES_PARAGRAPHS.add(BigInteger.class);
    }
    public static boolean requiresParagraphs(Class<?> clazz) { return !DOESNT_REQUIRES_PARAGRAPHS.contains(clazz); }

    public static boolean isTable(@NotNull Class<?> clazz) { return clazz.isArray() || Collection.class.isAssignableFrom(clazz); }

    public static boolean isMap(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz)
                || HashMap.class.isAssignableFrom(clazz)
                || LinkedHashMap.class.isAssignableFrom(clazz)
                || TreeMap.class.isAssignableFrom(clazz)
                || ConcurrentHashMap.class.isAssignableFrom(clazz)
                || Hashtable.class.isAssignableFrom(clazz)
                || EnumMap.class.isAssignableFrom(clazz)
                || WeakHashMap.class.isAssignableFrom(clazz)
                || IdentityHashMap.class.isAssignableFrom(clazz)
                || Properties.class.isAssignableFrom(clazz)
                || ConcurrentSkipListMap.class.isAssignableFrom(clazz)
                || ConcurrentNavigableMap.class.isAssignableFrom(clazz);
    }


    private final Map<Class<?>, Converter<?, ?>> create_class = new HashMap<>();
    private final Map<Class<?>, Converter<?, ?>> create_class_extending = new HashMap<>();
    private final Map<Class<?>, Converter<?, ?>> create_fields = new HashMap<>();
    private final Map<Class<?>, Converter<?, ?>> create_fields_extending = new HashMap<>();

    private final Map<Class<?>, Converter<?, Object>> convert_class = new HashMap<>();
    private final Map<Class<?>, Converter<?, Object>> convert_class_extending = new HashMap<>();
    private final Map<Field, Converter<?, Object>> convert_field = new HashMap<>();
    private final Map<Class<?>, Converter<?, Object>> convert_fields = new HashMap<>();
    private final Map<Class<?>, Converter<?, Object>> convert_field_extending = new HashMap<>();
    private final Map<Class<?>, List<Field>> excluded_fields = new HashMap<>();
    private final Map<Class<?>, Class<?>> process_class_as = new HashMap<>();
    private boolean auto_ex_f = true;
    private boolean ignore_processing = false;
    private boolean ign_np_v = true;
    private boolean log_c = false;
    private boolean urp = true;
    private int tree_prot = 100;
    private String spacing_string = "    ";
    private boolean pretty_up = false;

    public OODP() {}
    public OODP(boolean pretty) { this.pretty_up = pretty; }

    public void logConversions(boolean val) { log_c = val; }
    public void ignoreMissingValues(boolean val) { ign_np_v = val; }
    public void ignoreProcessing(boolean val) { ignore_processing = val; }
    public void urProtection(boolean val) { this.urp = val; }
    public void setPrettySpacingString(String space) { spacing_string = space; }
    public void treeProtection(int v) { tree_prot = v; }
    public void pretty(boolean v) { pretty_up = v; }

    public <I, T> void createClass(Class<I> in, Class<T> out, Function<I, T> func) { create_class.put(out, new Converter<>(in, out, func)); }
    public <T> void createClass(Class<T> out, Function<ObjectiveMap, T> func) { create_class.put(out, new Converter<>(ObjectiveMap.class, out, func)); }
    public <I, T> void createClassExtending(Class<I> in, Class<T> out, Function<I, T> func) { create_class_extending.put(out, new Converter<>(in, out, func)); }
    public <T> void createClassExtending(Class<T> out, Function<ObjectiveMap, T> func) { create_class_extending.put(out, new Converter<>(ObjectiveMap.class, out, func)); }

    public <I> void convertClass(Class<I> in, Function<I, Object> func) { convert_class.put(in, new Converter<>(in, Object.class, func)); }
    public <I> void convertClassExtending(Class<I> in, Function<I, Object> func) { convert_class_extending.put(in, new Converter<>(in, Object.class, func)); }


    public <I, T> void createFields(Class<I> in, Class<T> out, Function<I, T> func) { create_fields.put(out, new Converter<>(in, out, func)); }
    public <T> void createFields(Class<T> out, Function<ObjectiveMap, T> func) { create_fields.put(out, new Converter<>(ObjectiveMap.class, out, func)); }
    public <T> void createFieldsExtending(Class<T> out, Function<ObjectiveMap, T> func) { create_fields_extending.put(out, new Converter<>(ObjectiveMap.class, out, func)); }
    public <I, T> void createFieldsExtending(Class<I> in, Class<T> out, Function<I, T> func) { create_fields_extending.put(out, new Converter<>(in, out, func)); }

    public <I> void convertFields(Class<I> in, Function<I, Object> func) { convert_fields.put(in, new Converter<>(in, Object.class, func)); }
    public <I> void convertFieldsExtending(Class<I> in, Function<I, Object> func) { convert_field_extending.put(in, new Converter<>(in, Object.class, func)); }

    public void excludeFieldsFor(Class<?> clazz, String @NotNull ... field_names) {
        List<Field> fields = new ArrayList<>();
        for (String s : field_names)
            try {
                fields.add(getField(clazz, s));
            } catch (RuntimeException e) {
                if (clazz.getSuperclass() != null)
                    fields.add(getField(clazz.getSuperclass(), s));
                else throw new RuntimeException(e);
            }
        excludeFieldsFor(clazz, fields);
    }
    public static @NotNull Field getField(@NotNull Class<?> c, String name) {
        try {
            return c.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    public void excludeFieldsFor(Class<?> clazz, Field... fields) { excludeFieldsFor(clazz, Arrays.asList(fields)); }
    public void excludeFieldsFor(Class<?> clazz, List<Field> fields) {
        if (!excluded_fields.containsKey(clazz)) excluded_fields.put(clazz, new ArrayList<>());
        excluded_fields.put(clazz, fields);
    }
    public void excludeFieldsFor(String clazz, String @NotNull ... field_names) {
        try {
            excludeFieldsFor(Class.forName(clazz), field_names);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    public void excludeFieldsFor(String clazz, List<Field> fields) {
        try {
            excludeFieldsFor(Class.forName(clazz), fields);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public <I> void convertField(@NotNull Field field, Class<I> f_type, Function<I, Object> func) {
        if (field.getType() != f_type)
            throw new IllegalArgumentException("Class " + f_type.getName() + " is not field type of " + field.getDeclaringClass().getName( ) + "$" + field.getName());
        convert_field.put(field, new Converter<>(f_type, Object.class, func));
    }
    public <I> void convertField(@NotNull Class<?> clazz, String field, Class<I> f_type, Function<I, Object> func) {
        try {
            convertField(clazz.getDeclaredField(field), f_type, func);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public void processClassAs(Class<?> clazz, Class<?> as) {
        if (clazz == null)
            throw new RuntimeException("clazz can't be null");
        if (as == null)
            throw new RuntimeException("as class can't be null");
        if (as.isAssignableFrom(clazz)) process_class_as.put(clazz, as);
        else if (!ignore_processing)
            throw new RuntimeException(new ClassCastException(as.getName() + " can't be cast to " + clazz.getName()));
    }
    public void processClasAs(Class<?> clazz, String as) {
        try {
            processClassAs(clazz, Class.forName(as));
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    public void processClasAs(String clazz, Class<?> as) {
        try {
            processClassAs(Class.forName(clazz), as);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    public void processClasAs(String clazz, String as) {
        try {
            processClassAs(Class.forName(clazz), Class.forName(as));
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public void autoExcludeFields(boolean value) { auto_ex_f = value; }

    public List<Field> getFieldsFor(@NotNull Class<?> clazz) {
        List<Field> fields = new ArrayList<>(List.of(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null)
            fields.addAll(getFieldsFor(clazz.getSuperclass()));
        List<Field> excluded = excluded_fields.get(clazz);
        fields = fields.stream().filter(f -> !(f.isAnnotationPresent(OODPExclude.class) || (excluded != null && excluded.contains(f)))).toList();
        return fields;
    }

    //TODO fix when there are more that 10 objects in a Map<Object, ?>
    public <I, T> T parseTo(String raw, Class<T> clazz) {
        if (raw == null) return null;
        Converter<I, T> cv = (Converter<I, T>) or(create_class.get(clazz), create_class_extending.get(getFirstExtending(create_class_extending.keySet(), clazz)));
        if (cv != null) return cv.f.apply(parseTo(raw, cv.i));
        else if (isDefault(clazz)) {
            Object o = raw;
            if (clazz == String.class) o = raw.substring(1, raw.length()-1);
            else if (clazz == int.class || clazz == Integer.class) o = Integer.parseInt(raw);
            else if (clazz == double.class || clazz == Double.class) o = Double.parseDouble(raw);
            else if (clazz == long.class || clazz == Long.class) o = Long.parseLong(raw);
            else if (clazz == boolean.class || clazz == Boolean.class) o = Boolean.parseBoolean(raw);
            else if (clazz == float.class || clazz == Float.class) o = Float.parseFloat(raw);
            else if (clazz == char.class || clazz == Character.class) o = raw.charAt(0);
            else if (clazz == byte.class || clazz == Byte.class) o = Byte.parseByte(raw);
            else if (clazz == short.class || clazz == Short.class) o = Short.parseShort(raw);
            else if (clazz == UUID.class) o = UUID.fromString(raw);
            return (T) o;
        } else if (clazz == ObjectiveMap.class) {
            return (T) map(raw);
        } else return parseMapTo(map(raw), clazz);
    }

    public <I, T> T parseMapTo(ObjectiveMap om, Class<T> clazz) {
        if (om == null) return null;
        if (clazz == ObjectiveMap.class) return (T) om;

        Converter<I, T> cv = (Converter<I, T>) or(create_class.get(clazz), create_class_extending.get(getFirstExtending(create_class_extending.keySet(), clazz)));
        if (cv != null) return cv.f.apply(om.as(cv.i));
        else if (clazz == Object.class) return (T) om;
        else return omToClass(om, clazz);
    }

    private <I, T> @NotNull T omToClass(ObjectiveMap om, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field f : getFieldsFor(clazz)) {
                String fn = f.getName();
                if (!om.has(fn)) {
                    if (ign_np_v) continue;
                    else throw new RuntimeException("No key found for field " + fn + " of class " + clazz + " in provided map");
                }
                try {
                    f.setAccessible(true);
                    if (!Modifier.isStatic(f.getModifiers())) {
                        Class<?> ft = f.getType();
                        Converter<I, ?> fcv = (Converter<I, ?>) or(create_fields.get(ft), create_fields_extending.get(getFirstExtending(create_fields_extending.keySet(), ft)));
                        if (fcv != null) f.set(instance, fcv.f.apply(om.get(fn, fcv.i)));
                        else if (isTable(ft)) {
                            if (f.getGenericType() instanceof Class<?> cpt && cpt.getComponentType() != null)
                                f.set(instance, om.getObjectArray(fn, cpt.getComponentType()));
                            else if (f.getGenericType() instanceof ParameterizedType pt) {
                                Type type = pt.getActualTypeArguments()[0];
                                if (type instanceof ParameterizedType para_type)
                                    f.set(instance, morphListTo(om.getList(fn, para_type), ft));
                                else f.set(instance, morphListTo(om.getList(fn, type), ft));
                            }
                        } else f.set(instance, om.get(fn, ft));
                    }
                } catch (Exception e) {
                    if (auto_ex_f) excludeFieldsFor(clazz, f);
                    else throw new RuntimeException("Error occurred while setting field " + fn + " of class " + clazz, e);
                }
            }
            return instance;
        } catch (NoSuchMethodException e) {
            //TODO create instance from an existing constructor
            throw new RuntimeException("Class " + clazz.getName() + " doesn't have a default (parameter-less) constructor.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error creating or setting fields for class: " + clazz.getName(), e);
        }
    }

    public static <T> Object morphListTo(List<T> list, Class<?> clazz) {
        if (clazz == Set.class) return new HashSet<>(list);
        return list;
    }

    //Converting objects to strings
    public static class MalformedOODPException extends Exception {  public MalformedOODPException(String s) { super(s); } }

    public boolean saveToFile(Object o, @NotNull File f) { return saveToFile(o, f, pretty_up); }
    public boolean saveToFile(Object o, @NotNull File f, boolean pretty) {
        byte[] data;
        if (o instanceof byte[] ba) data = ba;
        else if (o instanceof String s) data = s.getBytes();
        else data = idp(o, pretty).getBytes();
        try {
            if (!f.exists() && (!f.createNewFile())) return false;
            try (FileOutputStream outputStream = new FileOutputStream(f)) { outputStream.write(data); }
            return true;
        } catch (IOException e) { return false; }
    }

    public Class<?> getFirstExtending(@NotNull Set<Class<?>> set, Class<?> clazz) {
        for (Class<?> kc : set)
            if (kc.isAssignableFrom(clazz))
                return kc;
        return clazz;
    }

    private String separator = "|   ";

    private final Set<Object> object_tree = Collections.newSetFromMap(new IdentityHashMap<>());
    private int tree_d = 0;

    int err_c = 0;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("[HH:mm:ss]");
    private @NotNull String time() { return "[" + LocalTime.now().format(dtf) + "]"; }

    public @NotNull String toOodp(Object o) { return toOodp(o, pretty_up, false); }
    public @NotNull String toOodp(Object o, boolean pretty) { return toOodp(o, pretty, false); }
    public @NotNull String toOodp(Object o, boolean pretty, boolean auto) {
        String r = auto ? classToString(o.getClass(), o) : idp(o, pretty);
        err_c = 0;
        return r;
    }
    private <I, T> @NotNull String idp(Object o, boolean pretty) {
        if (o == null) return "null";

        if (err_c > 10) {
            throw new RuntimeException(time() + " [OODP/Warn] More that 10 errors detected during conversion. Class sequence:\n\t" + String.join("\n\t", object_tree.stream().map(ob -> ob.getClass().getName() + " -->").toList()) + "\nStacktrace:");
        }

        tree_d++;

        if (tree_prot > 0 && tree_d >= tree_prot) {
            err_c++;
            System.out.println(time() + " [OODP/Warn] The function \"internalToOodp(Object)\" has called itself " + tree_d + " times, which triggered this protection. Returning null for object " + o.getClass());
            tree_d--;
            return "null";
        }

        if (urp) {
            if (!object_tree.add(o)) {
                err_c++;
                object_tree.remove(o);
                System.err.println(time() + " [OODP/Warn] A unbounded recursion was detected in the \"internalToOodp(Object)\" function for "+o.getClass().getSimpleName()+". This check is in place to prevent a stack overflow. " +
                        "You can disable this check by calling \"urProtection(false)\"");
                tree_d--;
                return "null";
            }
        }

        try {

            Class<T> c = (Class<T>) o.getClass();

            if (log_c) System.err.println(separator.repeat(tree_d)+"Converting " + c + " " + o + " to String:");

            if (process_class_as.containsKey(c))
                c = (Class<T>) process_class_as.get(c);

            String s;

            Converter<I, T> cv = (Converter<I, T>) or(convert_class.get(c), convert_class_extending.get(getFirstExtending(convert_class_extending.keySet(), c)));
            if (cv != null) {
                if (log_c) System.out.println(separator.repeat(tree_d)+"Found custom conversion for " + c);
                s = idp(cv.f.apply((I) o), false);
            } else if (isDefault(c) || isStringable(c)) {
                s = String.valueOf(o);
                if (requiresParagraphs(c) && (!s.startsWith("{") || !s.endsWith("}"))) s =  "\"" + s + "\"";
            } else if (o instanceof Enum<?> e) s = e.name();
            else if (isTable(c)) s = arrayToOodp(o, false);
            else if (isMap(c)) s = mapToOodp(o, false);
            else s = classToString(c, o);
            return pretty ? prettyUp(s) : s;
        } finally {
            tree_d--;
            object_tree.remove(o);
        }
    }

    private  <T> String classToString(Class<T> c, Object o) {
        StringBuilder sb = new StringBuilder("{");
        for (Field field : getFieldsFor(c)) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.getName().startsWith("this$")) {
                try {
                    field.setAccessible(true);
                    sb.append(field.getName()).append('=');

                    if (log_c)
                        System.out.println(separator.repeat(tree_d) + "- Converting field " + field.getName() + ":");

                    Object fo = field.get(o);

                    Converter<T, Object> fcv = (Converter<T, Object>) convert_field.get(field);
                    if (fcv != null) sb.append(idp(fcv.f.apply((T) fo), false));
                    else {
                        Class<?> fc = field.getType();

                        fcv = (Converter<T, Object>) or(convert_fields.get(fc), convert_field_extending.get(getFirstExtending(convert_field_extending.keySet(), fc)));
                        if (fcv != null) fo = fcv.f.apply((T) fo);
                        sb.append(idp(fo, false));
                    }
                    sb.append(',');
                } catch (Exception e) {
                    excludeFieldsFor(c, field);
                    System.out.println(e.getMessage());
                }
            }
        }
        if (sb.length() > 1 && sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        return sb.append("}").toString();
    }

    private @NotNull String arrayToOodp(@NotNull Object array) { return arrayToOodp(array, pretty_up); }
    private @NotNull String arrayToOodp(@NotNull Object array, boolean pretty) {
        if (!isTable(array.getClass())) throw new IllegalArgumentException("Argument is neither an array nor a List/Set/Collection");
        Collection<Object> elements = (array instanceof Collection<?> collection)
                ? new ArrayList<>(collection)
                : null;
        if (elements == null) {
            int length = Array.getLength(array);
            elements = new ArrayList<>(length);
            for (int i = 0; i < length; i++)
                elements.add(Array.get(array, i));
        }

        StringBuilder sb = new StringBuilder("[");
        for (Object element : elements) sb.append(idp(element, false)).append(',');

        if (sb.length() > 1) sb.setCharAt(sb.length() - 1, ']');
        else sb.append(']');

        return pretty ? prettyUp(sb.toString()) : sb.toString();
    }

    //TODO maps are just... wierd. fix it
    private String mapToOodp(Object o) { return mapToOodp(o, pretty_up); }
    private String mapToOodp(Object o, boolean pretty) {
        if (o instanceof Map<?, ?> map) {
            if (map.isEmpty()) return "{}";
            StringBuilder sb = new StringBuilder("{");
            boolean sk = true;
            Class<?> keyc = map.entrySet().iterator().next().getKey().getClass();
            for (Map.Entry<?, ?> e : map.entrySet())
                if (e.getKey().getClass() != keyc) { sk = false; break; }
            if (sk) {
                for (Map.Entry<?, ?> e : map.entrySet())
                    sb.append(e.getKey()).append('=').append(idp(e.getValue(), false)).append(',');
            } else {
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    String k;
                    Class<?> c = e.getKey().getClass();
                    if (isDefault(c)) {
                        k = String.valueOf(e.getKey());
                    } else if (!isStringable(c) && !isTable(c) && !isMap(c)) {
                        k = '|' + c.getName() + '|' + idp(e.getKey(), false);
                    } else  k = idp(e.getKey(), false);
                    sb.append(k).append('=').append(idp(e.getValue(), false)).append(',');
                }
            }
            if (sb.length() > 1 && sb.charAt(sb.length()-1) != '}') sb.deleteCharAt(sb.length() - 1);
            sb.append('}');
            return pretty ? prettyUp(sb.toString()) : sb.toString();
        } else throw new IllegalArgumentException("Object is not a map");
    }

    public static @NotNull String clean(@NotNull String s) {
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray();
        int i = 0;
        boolean p = false;
        while (i < chars.length) {
            char cr = chars[i];
            if (!p) while (cr == ' ' || cr == '\t' || cr == '\n') {
                i++;
                cr = chars[i];
            }
            if (cr == '\"') p = !p;
            sb.append(cr);
            i++;
        }
        return sb.toString();
    }

    public static @NotNull List<String> smartSplit(@NotNull String s, char c) {
        List<String> sts = new ArrayList<>();
        char[] chars = s.toCharArray();
        int i = 0;
        boolean p = false;
        int a = 0;
        int o = 0;
        StringBuilder sb = new StringBuilder();
        while (i < chars.length) {
            if (!p) while (chars[i] == ' ') i++;
            char cr = chars[i];
            if (cr == '\"') p = !p;
            else if (!p) {
                if (cr == '{') o++;
                else if (cr == '}') o--;
                else if (cr == '[') a++;
                else if (cr == ']') a--;
            }
            if (cr != c || p || a != 0 || o != 0) sb.append(cr);
            if ((!p && o == 0 && a == 0 && cr == c)) {
                sts.add(sb.toString());
                sb = new StringBuilder();
            }
            i++;
        }
        if (!sb.isEmpty()) sts.add(sb.toString());
        return sts;
    }

    public ObjectiveMap map(File f) {
        if (f == null || !f.exists()) throw new RuntimeException("File " + (f == null ? "null" : f.getName()) + " doesn't exist");
        try {
            return this.map(new String(Files.readAllBytes(f.toPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectiveMap newMap() { return new ObjectiveMap(); }

    public @NotNull ObjectiveMap map(String s) {
        ObjectiveMap om = new ObjectiveMap();
        if (s == null || s.isEmpty()) return om;
        s = clean(s);
        if (s.charAt(0) == '{') {
            if (s.charAt(s.length()-1) == '}')
                s = s.substring(1, s.length()-1);
        } else if (s.charAt(0) == '[') {
            if (s.charAt(s.length()-1) == ']')
                s = s.substring(1, s.length()-1);
        }
        List<String> spl = smartSplit(s, ',');
        for (String st : spl) {
            String k;
            String v;
            if (st.startsWith("{") || st.startsWith("[") || st.startsWith("|")) {
                char endc = (st.startsWith("{") || st.startsWith("|")) ? '}' : ']';
                int i = 1;
                while (st.charAt(i) != endc) i++;
                k = st.substring(0, i + 2);
                v = st.substring(i + 3);
            } else {
                String[] parts = st.split("=", 2);
                k = parts[0];
                if (k.startsWith("\"")) k=k.substring(1, k.length()-1);
                v = parts.length > 1 ? parts[1] : "";
            }
            char[] chars = v.toCharArray();
            StringBuilder sb = new StringBuilder();
            int i = 0;
            boolean override = false;
            boolean p = false;
            boolean al = true;
            while (i < chars.length) {
                if (!p) while (chars[i] == ' ') i++;
                char cr = chars[i];
                if (al && cr == '{') {
                    if (chars[chars.length-1] != '}')
                        throw new RuntimeException(new MalformedOODPException(v));
                    override = true;
                    om.put(k, v);
                    break;
                } else if (al && cr == '[') {
                    if (chars[chars.length-1] != ']')
                        throw new RuntimeException(new MalformedOODPException(v));
                    override = true;
                    om.put(k, v);
                    break;
                } else { al = false; }
                if (cr == '\"') { p = !p; }
                sb.append(chars[i]);
                i++;
            }
            if (!override) om.put(k, sb.toString());
        }
        return om;
    }

    public @NotNull String prettyUp(@NotNull String oodp) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        boolean p = false;
        char cr;
        char[] c = oodp.toCharArray();
        int i = 0;
        while (i < c.length) {
            cr = c[i];
            if (!p) while (cr == ' ') {
                i++;
                cr = c[i];
            }
            if (cr == '\"') p = !p;
            if (p) sb.append(cr);
            else {
                if (cr == '=') sb.append(" = ");
                else if (cr == ',') sb.append(cr).append('\n').append(spacing_string.repeat(depth));
                else if (cr == '{' || cr == '[') {
                    depth++;
                    sb.append(cr).append('\n').append(spacing_string.repeat(depth));
                } else if ((cr == '}' || cr == ']') && depth > 0) {
                    depth--;
                    sb.append('\n').append(spacing_string.repeat(depth)).append(cr);
                } else sb.append(cr);
            }
            i++;
        }
        return sb.toString();
    }
}
