package esolutions.com.esdatabaselib.utils;

import java.util.HashSet;
import java.util.Set;

public class ArrayUtils {

    public static String[] build(Object... values) {
        String[] arr = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Boolean) {
                boolean value = (Boolean) values[i];
                arr[i] = String.valueOf(value ? 1 : 0);
            } else {
                arr[i] = String.valueOf(values[i]);
            }
        }
        return arr;
    }

    //region check class is primitive
    private static final Set<Class<?>> WRAPPER_TYPES = checkWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> checkWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    //endregion
}