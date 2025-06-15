package org.hetils.jgl17;

import org.hetils.jgl17.oodp.OODP;
import org.hetils.jgl17.oodp.OODPExclude;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public class Config extends HashSet<Config.Property<?>> {

    public static final OODP config_dp = new OODP(true);
    static {
        config_dp.excludeFieldsFor(Config.class, "file");
        config_dp.convertClass(Config.class, c -> {
            HashMap<String, Object> m = new HashMap<>();
            for (Property<?> p : c)
                m.put(p.prop, p.value);
            return m;
        });
    }

    public static class Property<T> {

        public static <T> void setValue(@NotNull Property<T> p, T val) { p.value = val; }

        public static <T> void setPropertyFromOm(OODP.@NotNull ObjectiveMap om, @NotNull Property<T> p) {
            try {
                p.value = om.get(p.prop, p.type);
            } catch (Exception e) {
                p.value = p.defVal;
            }
        }

        private final String prop;
        private final T defVal;
        private final Class<T> type;
        private T value;

        @Contract(pure = true)
        public Property(String property, @NotNull T value) {
            this.prop = property;
            this.defVal = value;
            this.value = value;
            this.type = (Class<T>) value.getClass();
        }

        public String prop() {
            return prop;
        }

        public T def() {
            return defVal;
        }

        @Override
        public String toString() {
            return value.toString();
        }

        public T val() {
            return value;
        }

        public Class<T> type() { return type; }

        public void setV(T value) {
            this.value = value;
        }
    }

    private Path file;
    public Config() { file = null; }
    public Config(String file) { this.file = Path.of(file); }
    public Config(Path file) { this.file = file; }

    public void setFile(Path file) { this.file = file; }

    public <T> boolean set(String prop, T val) {
        for (Property<?> p : this)
            if (Objects.equals(p.prop, prop)) {
                if (p.type == val.getClass()) ((Property<T>) p).setV(val);
                break;
            }
        return this.add(new Property<>(prop, val));
    }

    public boolean isSet(String property) {
        for (Property<?> p : this)
            if (Objects.equals(property, p.prop()))
                return true;
        return false;
    }

    public boolean is(String prop) { return Objects.equals(get(prop), true); }
    public boolean is(String prop, Object val) { return Objects.equals(get(prop), val); }

    public boolean remove(String prop) {
        Iterator<Property<?>> it = this.iterator();
        while (it.hasNext())
            if (prop.equals(it.next().prop)) {
                it.remove();
                return true;
            }
        return false;
    }

    public @Nullable Property<?> getProperty(String prop) {
        for (Property<?> p : this)
            if (Objects.equals(prop, p.prop()))
                return p;
        return null;
    }

    public @Nullable Object get(String prop) {
        for (Property<?> p : this)
            if (Objects.equals(prop, p.prop()))
                return p.val();
        return null;
    }

    public <T> @Nullable T get(String prop, Class<T> type) {
        for (Property<?> p : this)
            if (Objects.equals(prop, p.prop()) && p.val().getClass() == type)
                return (T) p.val();
        return null;
    }

    public boolean save()  {
        if (file != null) {
            try  {
                config_dp.saveToFile(this, file.toFile());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean load() {
        try {
            OODP.ObjectiveMap om = config_dp.map(file);
            for (Property<?> p : this)
                Property.setPropertyFromOm(om, p);
            main:
            for (String key : om.keySet()) {
                for (Property<?> p : this)
                    if (Objects.equals(p.prop, key))
                        continue main;
                this.set(key, om.getAuto(key));
            }
            return true;
        } catch (Exception e) {}
        return false;
    }

    public ArrayList<String> properties() {
        ArrayList<String> s = new ArrayList<>();
        for (Property<?> p : this)
            s.add(p.prop);
        return s;
    }

}

