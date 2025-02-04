package com.github.cichyvx.mapper;

public class MapperUtils {

    public static boolean isShort(Class<?> aClass) {
        return Short.class.equals(aClass) || short.class.equals(aClass);
    }

    public static boolean isByte(Class<?> aClass) {
        return Byte.class.equals(aClass) || byte.class.equals(aClass);
    }

    public static boolean isFloat(Class<?> aClass) {
        return Float.class.equals(aClass) || float.class.equals(aClass);
    }

    public static boolean isCharacter(Class<?> aClass) {
        return Character.class.equals(aClass) || char.class.equals(aClass);
    }

    public static boolean isBoolean(Class<?> aClass) {
        return Boolean.class.equals(aClass) || boolean.class.equals(aClass);
    }

    public static boolean isDouble(Class<?> aClass) {
        return Double.class.equals(aClass) || double.class.equals(aClass);
    }

    public static boolean isLong(Class<?> aClass) {
        return Long.class.equals(aClass) || long.class.equals(aClass);
    }

    public static boolean isInt(Class<?> aClass) {
        return Integer.class.equals(aClass) || int.class.equals(aClass);
    }

    public static boolean isEnum(Class<?> aClass) {
        return Enum.class.isAssignableFrom(aClass);
    }

    public static boolean isPrimitiveOrString(Class<?> aClass) {
        return MapperUtils.isShort(aClass)
                || MapperUtils.isByte(aClass)
                || MapperUtils.isFloat(aClass)
                || MapperUtils.isDouble(aClass)
                || MapperUtils.isCharacter(aClass)
                || MapperUtils.isBoolean(aClass)
                || MapperUtils.isLong(aClass)
                || MapperUtils.isInt(aClass)
                || MapperUtils.isFloat(aClass)
                || aClass == String.class;
    }

    static Object sneakyInvoke(Class<?> fieldClass, String fieldName, Object instance) {
        try {
            return fieldClass.getMethod(fieldName).invoke(instance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
