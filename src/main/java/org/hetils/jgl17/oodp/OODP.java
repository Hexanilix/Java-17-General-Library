package org.hetils.jgl17.oodp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

//TODO create readable oodp strings (Object Oriented Data Preserving)
public class OODP {
    public static  <T> T cast(Class<T> ignore, Object obj) throws ClassCastException { return (T) obj; }

    //TODO add get...Or methods
    public class ObjectiveMap extends HashMap<String, Object> {
        //omfg THANK GOD FOR CHATGPT without em I would not have figured of how tf component classes in arrays work,
        // although I had to figure out myself that if Class<?> is a List it doesn't retain its component
        // and you gotta pass a ParameterizedType instead said Class<?>. The more you know

        public ObjectiveMap() { super(); }
        public ObjectiveMap(Map<? extends String, ?> m) { super(m); }
        public ObjectiveMap(String key, Object value) { super(); this.put(key, value); }

        public interface MapToObjectFunction<T> { T create(ObjectiveMap om); }

        public boolean has(String key) { return this.containsKey(key); }

        public <T> T as(@NotNull ObjectiveMap.MapToObjectFunction<T> func) { return func.create(this); }

        public <T> T as(Class<T> clazz) { return mapAsClass(this, clazz); }

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
                    map.put((K) stringAs(k.substring(0, k.length()-1), c), this.stringAs(this.getString(key), valc));
                } else {
                    map.put(stringAs(key.substring(0, key.length()-1), keyc), this.stringAs(this.getString(key), valc));
                }
            }
            return map;
        }

        /**
         * This function simply return the wrapper type for primitive classes
         *
         * @return wrapper type for {clazz}
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

        public  <T> T stringAs(String str, Class<T> clazz) {
            Object o;
            if (isDefault(clazz)) {
                if (clazz == String.class) o = str;
                else if (clazz == int.class || clazz == Integer.class) o = Integer.parseInt(str);
                else if (clazz == double.class || clazz == Double.class) o = Double.parseDouble(str);
                else if (clazz == long.class || clazz == Long.class) o = Long.parseLong(str);
                else if (clazz == boolean.class || clazz == Boolean.class) {
                    if (str.equalsIgnoreCase("true")) o = true;
                    else if (str.equalsIgnoreCase("false")) o = false;
                    else throw new RuntimeException(new ClassCastException("java.lang.String can't be cast to boolean"));
                }
                else if (clazz == float.class || clazz == Float.class) o = Float.parseFloat(str);
                else if (clazz == char.class || clazz == Character.class) o = str.charAt(0);
                else if (clazz == byte.class || clazz == Byte.class) o = Byte.parseByte(str);
                else if (clazz == short.class || clazz == Short.class) o = Short.parseShort(str);
                else if (clazz == UUID.class) o = UUID.fromString(str);
                else return null;
                if (clazz.equals(o.getClass()) || getWrapperType(clazz) == o.getClass()) return (T) o;
                else throw new RuntimeException(o.getClass().getName() + " is not an instance of " + clazz.getName());
            } else if (clazz == Object.class) {
                for (Class<?> c : DEFAULT_CLASSES) {
                    try {
                        return (T) stringAs(str, c);
                    } catch (RuntimeException ignore) {}
                }
                return null;
            } else return map(str).as(clazz);
        }

        public <T> T get(String key, Class<T> clazz) {
            Object o;
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
                    else o = getAs(key, clazz);
                    if (clazz.equals(o.getClass()) || getWrapperType(clazz) == o.getClass()) return (T) o;
                    else throw new RuntimeException(o.getClass().getName() + " is not an instance of " + clazz.getName());
                } else if (clazz.isEnum()) {
                    String enumName = getString(key);
                    try {
                        return (T) Enum.valueOf((Class<Enum>) clazz, enumName);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("No enum constant " + clazz.getName() + "." + enumName, e);
                    }
                } else return mapAsClass(this.getObjectiveMap(key), clazz);
            }
            return null;
        }

        public static <T extends Enum<T>> T getEnumValue(Class<T> enumClass, String name) {
            for (T constant : enumClass.getEnumConstants())
                if (constant.name().equalsIgnoreCase(name))
                    return constant;
            return null;
        }


        public <T> Object getArr(String key, Class<T> clazz) {
            if (clazz == String.class) return  getStringArr(key);
            else if (clazz == int.class || clazz == Integer.class) return getIntArr(key);
            else if (clazz == double.class || clazz == Double.class) return getDoubleArr(key);
            else if (clazz == long.class || clazz == Long.class) return getLongArr(key);
            else if (clazz == boolean.class || clazz == Boolean.class) return getBoolean(key);
            else if (clazz == float.class || clazz == Float.class) return getFloat(key);
            else if (clazz == char.class || clazz == Character.class) return getString(key);
            else if (clazz == byte.class || clazz == Byte.class) return getByteArr(key);
            else if (clazz == short.class || clazz == Short.class) return getShortArr(key);
            else if (clazz == UUID.class) return getUUIDArr(key);
            else return getObjectArr(key, clazz);
        }

        public <T> T[] getObjectArr(String key, Class<T> clazz) {
            ObjectiveMap om = this.getObjectiveMap(key);
            T[] arr = (T[]) Array.newInstance(clazz, om.size());
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.get(String.valueOf(i), clazz);
            return arr;
        }
        public Object[] getObjectArr(String key) {
            ObjectiveMap om = this.getObjectiveMap(key);
            Object[] arr = new Object[om.size()];
            for (int i = 0; i < om.size(); i++)
                for (Class<?> c : DEFAULT_CLASSES)
                    try {
                        arr[i] = cast(c, om.get(String.valueOf(i)));
                        break;
                    } catch (ClassCastException ignored) {}
            return arr;
        }

        public String getString(String s) { return (String) this.get(s); }
        public String[] getStringArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            String[] arr = new String[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getString(String.valueOf(i));
            return arr;
        }
        public List<String> getStringList(String s) { return Arrays.asList(((String) this.get(s)).split(",")); }

        public boolean getBoolean(String s) { return Boolean.parseBoolean((String) this.get(s)); }
        public boolean[] getBooleanArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            boolean[] arr = new boolean[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getBoolean(String.valueOf(i));
            return arr;
        }

        public int getInt(String s) { return Integer.parseInt((String) this.get(s)); }
        public int[] getIntArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            int[] arr = new int[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getInt(String.valueOf(i));
            return arr;
        }

        public long getLong(String s) { return Long.parseLong((String) this.get(s)); }
        public long[] getLongArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            long[] arr = new long[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getLong(String.valueOf(i));
            return arr;
        }

        public float getFloat(String s) { return Float.parseFloat((String) this.get(s)); }
        public float[] getFloatArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            float[] arr = new float[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getFloat(String.valueOf(i));
            return arr;
        }

        public double getDouble(String s) { return Double.parseDouble((String) this.get(s)); }
        public double[] getDoubleArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            double[] arr = new double[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getDouble(String.valueOf(i));
            return arr;
        }

        public short getShort(String s) { return Short.parseShort((String) this.get(s)); }
        public short[] getShortArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            short[] arr = new short[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getShort(String.valueOf(i));
            return arr;
        }

        public byte getByte(String s) { return Byte.parseByte((String) this.get(s)); }
        public byte[] getByteArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            byte[] arr = new byte[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getByte(String.valueOf(i));
            return arr;
        }

        public char getChar(String s) {
            String value = (String) this.get(s);
            if (value.length() != 1)
                throw new IllegalArgumentException("String value is not a single character= " + value);
            return value.charAt(0);
        }

        public UUID getUUID(String s) { return UUID.fromString((String) this.get(s)); }
        public UUID[] getUUIDArr(String s) {
            ObjectiveMap om = this.getObjectiveMap(s);
            UUID[] arr = new UUID[om.size()];
            for (int i = 0; i < om.size(); i++)
                arr[i] = om.getUUID(String.valueOf(i));
            return arr;
        }

        public java.math.BigInteger getBigInteger(String s) { return new java.math.BigInteger((String) this.get(s)); }

        public java.math.BigDecimal getBigDecimal(String s) { return new java.math.BigDecimal((String) this.get(s)); }

        public ObjectiveMap getObjectiveMap(String s) { return (ObjectiveMap) this.get(s); }

        public <T> T getAs(String s, @NotNull ObjectiveMap.MapToObjectFunction<T> func) { return func.create(this.getObjectiveMap(s)); }
        public <T> T getAs(String key, Class<T> clazz) {
            if (isDefault(clazz)) return get(key, clazz);
            else if (this.get(key) instanceof ObjectiveMap om) return mapAsClass(om, clazz);
            else return mapAsClass(new ObjectiveMap("value", this.get(key)), clazz);
        }

        public <T> List<T> getObjectList(String key, MapToObjectFunction<T> func) {
            ObjectiveMap om = this.getObjectiveMap(key);
            List<T> arr = new ArrayList<>();
            for (int i = 0; i < om.size(); i++)
                arr.add(om.getAs(String.valueOf(i), func));
            return arr;
        }
        public <T> List<T> getObjectList(String key, Class<T> clazz) {
            ObjectiveMap om = this.getObjectiveMap(key);
            List<T> arr = new ArrayList<>();
            if (isTable(clazz))
                for (int i = 0; i < om.size(); i++) arr.add((T) om.getObjectList(String.valueOf(i), (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getRawType()));
            else for (int i = 0; i < om.size(); i++)
                arr.add(om.getAs(String.valueOf(i), clazz));
            return arr;
        }
        public <T> List<T> getObjectList(String key, @NotNull ParameterizedType clazz) {
            ObjectiveMap om = this.getObjectiveMap(key);
            List<T> arr = new ArrayList<>();
            if (isTable((Class<?>) clazz.getRawType()))
                for (int i = 0; i < om.size(); i++) arr.add((T) om.getObjectList(String.valueOf(i), clazz.getActualTypeArguments()[0]));
            else for (int i = 0; i < om.size(); i++)
                arr.add((T) om.getAs(String.valueOf(i), (Class<?>) clazz.getRawType()));
            return arr;
        }
        public <T> List<T> getObjectList(String key, Type clazz) {
            ObjectiveMap om = this.getObjectiveMap(key);
            List<T> arr = new ArrayList<>();
            for (int i = 0; i < om.size(); i++)
                arr.add((T) om.getAs(String.valueOf(i), (Class<?>) clazz));
            return arr;
        }

        public List<ObjectiveMap> getObjectiveList(String key) {
            ObjectiveMap om = this.getObjectiveMap(key);
            List<ObjectiveMap> arr = new ArrayList<>();
            for (int i = 0; i < om.size(); i++)
                arr.add(om.getObjectiveMap(String.valueOf(i)));
            return arr;
        }
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

    public interface ObjectToOODPFunction <T> { Object convert(T obj); }
    public interface ObjectToHashMapFunction <T> extends ObjectToOODPFunction<T> {
        HashMap<String, Object> map(T obj);
        @Override
        default Object convert(T obj) { return map(obj); }
    }

    private final Map<Class<?>, ObjectiveMap.MapToObjectFunction<?>> create_functions = new HashMap<>();
    private final Map<Class<?>, ObjectiveMap.MapToObjectFunction<?>> create_extending_functions = new HashMap<>();
    private final Map<Class<?>, ObjectToOODPFunction<?>> convert_functions = new HashMap<>();
    private final Map<Class<?>, ObjectToOODPFunction<?>> extending_convert_functions = new HashMap<>();
    private final Map<Class<?>, List<Field>> excluded_fields = new HashMap<>();
    private final Map<Class<?>, Class<?>> process_as_class = new HashMap<>();
    private final Map<Class<?>, ObjectToOODPFunction<?>> extending_field = new HashMap<>();
    private CustomCheckFunction ccf = null;
    private boolean auto_ex_f = true;
    private boolean ignore_processing = false;
    private boolean omit_missing_values = true;
    private boolean log_c = false;

    public OODP() {}

    public void logConversions(boolean val) { log_c = val; }
    public void omitMissingValues(boolean val) { omit_missing_values = val; }
    public void ignoreProcessing(boolean val) { ignore_processing = val; }

    public interface CustomCheckFunction { Class<?> processAs(Object o); }

    public void customCheck(CustomCheckFunction c) { ccf = c; }

    public <T> void createClassFunc(Class<T> clazz, ObjectiveMap.@NotNull MapToObjectFunction<T> func) { create_functions.put(clazz, func); }
    public <T> void createClassFunc(String clazz, ObjectiveMap.@NotNull MapToObjectFunction<T> func) {
        try {
            createClassFunc((Class<T>) Class.forName(clazz), func);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public <T> void createClassExtendingFunc(Class<T> clazz, ObjectiveMap.@NotNull MapToObjectFunction<T> func) { create_extending_functions.put(clazz, func); }

    public <T> void convertClassFunc(Class<T> clazz, ObjectToOODPFunction<T> func) { convert_functions.put(clazz, func); }
    public <T> void hashConvertClassFunc(Class<T> clazz, ObjectToHashMapFunction<T> func) { convert_functions.put(clazz, func); }
    public <T> void convertClassFunc(String clazz, ObjectToOODPFunction<T> func) {
        try {
            convertClassFunc((Class<T>) Class.forName(clazz), func);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    public <T> void hashConvertClassFunc(String clazz, ObjectToHashMapFunction<T> func) {
        try {
            convertClassFunc((Class<T>) Class.forName(clazz), func);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public <T> void convertClassExtendingFunc(Class<T> clazz, ObjectToOODPFunction<T> func) { extending_convert_functions.put(clazz, func); }
    public <T> void hashConvertClassExtendingFunc(Class<T> clazz, ObjectToHashMapFunction<T> func) { extending_convert_functions.put(clazz, func); }
    public <T> void convertClassExtendingFunc(String clazz, ObjectToOODPFunction<T> func) {
        try {
            convertClassExtendingFunc((Class<T>) Class.forName(clazz), func);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    public <T> void hashConvertClassExtendingFunc(String clazz, ObjectToHashMapFunction<T> func) {
        try {
            hashConvertClassExtendingFunc((Class<T>) Class.forName(clazz), func);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public void excludeFieldsFor(Class<?> clazz, String @NotNull ... field_names) {
        List<Field> fields = new ArrayList<>();
        for (String s : field_names)
            try {
                fields.add(clazz.getDeclaredField(s));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        excludeFieldsFor(clazz, fields);
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

    public void processAs(Class<?> clazz, Class<?> as) {
        if (clazz == null)
            throw new RuntimeException("clazz can't be null");
        if (as == null)
            throw new RuntimeException("as class can't be null");
        if (as.isAssignableFrom(clazz)) process_as_class.put(clazz, as);
        else if (!ignore_processing)
            throw new RuntimeException(new ClassCastException(as.getName() + " can't be cast to " + clazz.getName()));
    }
    public void processAs(Class<?> clazz, String as) {
        try {
            processAs(clazz, Class.forName(as));
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    public void processAs(String clazz, Class<?> as) {
        try {
            processAs(Class.forName(clazz), as);
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    public void processAs(String clazz, String as) {
        try {
            processAs(Class.forName(clazz), Class.forName(as));
        } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public <T> void processFieldsExtendingClass(Class<T> clazz, ObjectToOODPFunction<T> func) { extending_field.put(clazz, func); }

    public void autoExcludeFields(boolean value) { auto_ex_f = value; }

    public List<Field> getFieldsFor(@NotNull Class<?> clazz) {
        List<Field> fields = new ArrayList<>(List.of(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null)
            fields.addAll(getFieldsFor(clazz.getSuperclass()));
        List<Field> excluded = excluded_fields.get(clazz);
        fields = fields.stream().filter(f -> !(f.isAnnotationPresent(OODPExclude.class) ||  (excluded != null && excluded.contains(f)))).toList();
        return fields;
    }

    //TODO fix when there are more that 10 objects in a Map<Object, ?>
    public <T> T mapAsClass(ObjectiveMap om, Class<T> clazz) {
        if (om == null) return null;
        if (create_functions.containsKey(clazz)) {
            return (T) create_functions.get(clazz).create(om);
        } else if (getFirstExtending(create_extending_functions.keySet(), clazz) != null) {
            return (T) create_extending_functions.get(getFirstExtending(create_extending_functions.keySet(), clazz)).create(om);
        } else {
            if (clazz == Object.class) return (T) om;
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field f : getFieldsFor(clazz)) {
                    String fn = f.getName();
                    if (!om.has(fn)) {
                        if (omit_missing_values) continue;
                        else throw new RuntimeException("No key found for field " + fn + " of class " + clazz + " in provided map");
                    }
                    try {
                        f.setAccessible(true);
                        if (!Modifier.isStatic(f.getModifiers())) {
                            Class<?> ft = f.getType();
                            if (isTable(ft)) {
                                if (f.getGenericType() instanceof Class<?> cpt && cpt.getComponentType() != null)
                                    f.set(instance, om.getArr(fn, cpt.getComponentType()));
                                else {
                                    Type type = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
                                    if (type instanceof ParameterizedType para_type)
                                        f.set(instance, om.getObjectList(fn, para_type));
                                    else f.set(instance, om.getObjectList(fn, type));
                                }
                            } else f.set(instance, om.get(fn, ft));
                        }
                    } catch (ClassCastException e) {
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
    }

    //Converting objects to strings
    public static class MalformedOODPException extends Exception {  public MalformedOODPException(String s) { super(s); } }

    public boolean saveToFile(Object o, @NotNull File f) {
        byte[] data;
        if (o instanceof byte[] ba) data = ba;
        else if (o instanceof String s) data = s.getBytes();
        else data = toOodp(o).getBytes();
        try {
            if (!f.exists()) f.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(f);
            outputStream.write(data);
            return true;
        } catch (IOException e) { return false; }
    }

    public Class<?> getFirstExtending(@NotNull Set<Class<?>> set, Class<?> clazz) {
        for (Class<?> kc : set)
            if (kc.isAssignableFrom(clazz))
                return kc;
        return null;
    }

    private int tree_d = 0;
    public <T> @NotNull String toOodp(Object o) {
        if (o == null) return "null";
        Class<T> c = (Class<T>) o.getClass();

        if (log_c) System.out.println("|\t".repeat(tree_d)+"Converting " + c + " to String:");
        tree_d++;
        if (ccf != null) {
            Class<?> rc = ccf.processAs(o);
            if (rc != null) c = (Class<T>) rc;
        }

        if (process_as_class.containsKey(c))
            c = (Class<T>) process_as_class.get(c);

        String s;
        if (convert_functions.containsKey(c)) {
            ObjectToOODPFunction<T> func = (ObjectToOODPFunction<T>) convert_functions.get(c);
            if (func instanceof ObjectToHashMapFunction<T> hash)
                s = toOodp(hash.convert((T) o));
            else s = toOodp(func.convert((T) o));
        }
        else if (getFirstExtending(extending_convert_functions.keySet(), c) != null) {
            c = (Class<T>) getFirstExtending(extending_convert_functions.keySet(), c);
            ObjectToOODPFunction<T> func = (ObjectToOODPFunction<T>) extending_convert_functions.get(c);
            if (func instanceof ObjectToHashMapFunction<T> hash)
                s = toOodp(hash.convert((T) o));
            else s = toOodp(func.convert((T) o));
        }
        else if (isDefault(c) || isStringable(c)) {
            if (requiresParagraphs(c)) s = "\"" + o + "\"";
            else s = String.valueOf(o);
        }
        else if (o instanceof Enum<?> e) s = e.name();
        else if (isTable(c)) s = arrayToOodp(o);
        else if (isMap(c)) s = mapToOodp(o);
        else {
            StringBuilder sb = new StringBuilder("{");
            for (Field field : getFieldsFor(c)) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.getName().startsWith("this$")) {
                    tree_d++;
                    try {
                        field.setAccessible(true);

                        if (log_c)
                            System.out.println("|\t".repeat(tree_d) + "- Converting field " + field.getName() + ":");

                        sb.append(field.getName()).append('=');
                        Object fo = field.get(o);
                        Class<?> fc = getFirstExtending(extending_field.keySet(), field.getType());
                        if (fc != null) {
                            ObjectToOODPFunction<T> func = (ObjectToOODPFunction<T>) extending_field.get(fc);
                            if (func instanceof ObjectToHashMapFunction<T> hash)
                                fo = hash.convert((T) fo);
                            else fo = func.convert((T) fo);
                        }
                        sb.append(toOodp(fo));
                        sb.append(',');

                        tree_d--;
                    } catch (IllegalAccessException e) {
                        tree_d--;
                        if (auto_ex_f) excludeFieldsFor(c, field);
                        else {
                            tree_d = 0;
                            throw new RuntimeException(e);
                        }
                    }

                }
            }
            if (sb.charAt(sb.length() - 1) != '}') sb.deleteCharAt(sb.length() - 1);
            s = sb.append("}").toString();
        }
        tree_d--;
        return s;
    }

    public @NotNull String arrayToOodp(@NotNull Object array) {
        StringBuilder sb = new StringBuilder("[");
        if (array instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++)
                sb.append(i).append('=').append(toOodp(list.get(i))).append(',');
        } else if (array.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(array); i++)
                sb.append(i).append('=').append(toOodp(Array.get(array, i))).append(',');
        } else throw new IllegalArgumentException("Argument is neither an array nor a List");

        if (sb.length() > 1 && sb.charAt(sb.length()-1) != ']') sb.deleteCharAt(sb.length() - 1);
        return sb.append("]").toString();
    }

    //TODO maps are just... wierd. fix it
    public String mapToOodp(Object o) {
        if (o instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean sk = true;
            Class<?> keyc = map.entrySet().iterator().next().getKey().getClass();
            for (Map.Entry<?, ?> e : map.entrySet())
                if (e.getKey().getClass() != keyc) { sk = false; break; }
            int i = 0;
            if (sk) {
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    sb.append(e.getKey()).append('=').append(toOodp(e.getValue())).append(',');
                    i++;
                }
            } else {
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    String k;
                    Class<?> c = e.getKey().getClass();
                    if (isDefault(c)) {
                        k = String.valueOf(e.getKey());
                    } else if (!isStringable(c) && !isTable(c) && !isMap(c)) {
                        k = '|' + c.getName() + '|' + toOodp(e.getKey());
                    } else  k = toOodp(e.getKey());
                    sb.append(k).append('=').append(toOodp(e.getValue())).append(',');
                    i++;
                }
            }
            if (sb.length() > 1 && sb.charAt(sb.length()-1) != '}') sb.deleteCharAt(sb.length() - 1);
            return sb.append('}').toString();
        } else throw new RuntimeException(new IllegalArgumentException("Object is not a map"));
    }

    public @NotNull String clean(@NotNull String s) {
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

    public @NotNull List<String> smartSplit(@NotNull String s, char c) {
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

    public ObjectiveMap map(@NotNull File f) {
        try {
            String input = new String(Files.readAllBytes(f.toPath()));
            String clean_input = clean(input);
            return this.map(clean_input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull ObjectiveMap map(@NotNull String s) {
        ObjectiveMap om = new ObjectiveMap();
        if (s.isEmpty()) return om;
        if (s.charAt(0) == '{') {
            if (s.charAt(s.length()-1) == '}')
                s = s.substring(1, s.length()-1);
//            else throw new RuntimeException(new MalformedOODPException(s));
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
                    om.put(k, map(v.substring(1, v.length()-1)));
                    break;
                } else if (al && cr == '[') {
                    if (chars[chars.length-1] != ']')
                        throw new RuntimeException(new MalformedOODPException(v));
                    override = true;
                    om.put(k, map(v.substring(1, v.length()-1)));
                    break;
                } else { al = false; }
                if (cr == '\"') { p = !p; }
                else sb.append(chars[i]);
                i++;
            }
            if (!override) om.put(k, sb.toString());
        }
        return om;
    }

}
