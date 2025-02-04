package com.github.cichyvx.mapper;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class ObjectMapper {

    private final JsonToPropertyMapper jsonToPropertyMapper;

    public ObjectMapper(JsonToPropertyMapper jsonToPropertyMapper) {
        this.jsonToPropertyMapper = jsonToPropertyMapper;
    }

    public String toJson(Object object) {
        if (object == null) {
            return Strings.EMPTY;
        }
        try {
            StringBuilder builder = new StringBuilder("{");

            Class<?> objectClass = object.getClass();
            Field[] declaredFields = objectClass.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                String fieldName = field.getName();
                writeKeyPropertyWithColon(builder, fieldName);
                if (filedIsArray(field)) {
                    writeFieldAsArray(object, builder, field, objectClass, fieldName);
                } else {
                    writeFieldAsSingleValue(object, field, builder, objectClass, fieldName);
                }

                if (hasMoreFields(i, declaredFields)) {
                    builder.append(",");
                }
            }
            endJson(builder);

            return builder.toString();
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private  <T> T fromJson(Map<String, ParsedProperty> parsed, Class<T> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, Pair<ParsedProperty, Class<?>>> matchedConstructorArgs = null;
        Constructor<T> ctor = null;
        for (var constructor : clazz.getConstructors()) {
            Map<String, ParsedProperty> clonedParsed = new HashMap<>(parsed);
            matchedConstructorArgs = new LinkedHashMap<>();
            boolean isMatchedConstructor = true;
            for (var parameter : constructor.getParameters()) {
                if (clonedParsed.containsKey(parameter.getName())) {
                    ParsedProperty x = clonedParsed.remove(parameter.getName());
                    matchedConstructorArgs.put(parameter.getName(), new Pair<>(x, parameter.getType()));

                } else {
                    isMatchedConstructor = false;
                    break;
                }
            }

            if (isMatchedConstructor) {
                ctor = (Constructor<T>) constructor;
                break;
            }

        }

        if (matchedConstructorArgs == null || ctor == null) {
            throw new ParsingException("Could not find matching constructor");
        }

        List<Object> constructorArguments = new LinkedList<>();

        matchedConstructorArgs.entrySet().forEach(entry -> fillArgs(entry, constructorArguments));

        return ctor.newInstance(constructorArguments.toArray());
    }

    private void fillArgs(Map.Entry<String, Pair<ParsedProperty, Class<?>>> entry, List<Object> args) {
        Class<?> argClass = entry.getValue().right;
        ParsedProperty parsedProperty = entry.getValue().left;
        boolean isArray = argClass.isArray();

        if (isArray) {
            argClass = argClass.getComponentType();
        }
        if (parsedProperty.isNested()) {
            processNestedArg(args, parsedProperty, argClass);
        } else {
            if (isArray) {
                processArraySimpleArg(args, argClass, parsedProperty);
            } else {
                processSingleSimpleArg(args, argClass, parsedProperty);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void processArraySimpleArg(List<Object> args, Class<?> argClass, ParsedProperty parsedProperty) {
        if (String.class.equals(argClass)) {
            args.add(parsedProperty.getValue().toArray(String[]::new));
        } else if (MapperUtils.isInt(argClass)) {
            args.add(parsedProperty.getValue().stream().mapToInt(Integer::parseInt).toArray());
        } else if (MapperUtils.isLong(argClass)) {
            args.add(parsedProperty.getValue().stream().mapToLong(Long::parseLong).toArray());
        } else if (MapperUtils.isDouble(argClass)) {
            args.add(parsedProperty.getValue().stream().mapToDouble(Double::parseDouble).toArray());
        } else if (MapperUtils.isBoolean(argClass)) {
            boolean[] array = new boolean[parsedProperty.getValue().size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = Boolean.parseBoolean(parsedProperty.getValue().get(i));
            }
            args.add(array);
        } else if (MapperUtils.isCharacter(argClass)) {
            char[] array = new char[parsedProperty.getValue().size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = parsedProperty.getValue().get(i).charAt(0);
            }
            args.add(array);
        } else if (MapperUtils.isFloat(argClass)) {
            float[] array = new float[parsedProperty.getValue().size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = Float.parseFloat(parsedProperty.getValue().get(i));
            }
            args.add(array);
        } else if (MapperUtils.isByte(argClass)) {
            byte[] array = new byte[parsedProperty.getValue().size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = Byte.parseByte(parsedProperty.getValue().get(i));
            }
            args.add(array);
        } else if (MapperUtils.isShort(argClass)) {
            short[] array = new short[parsedProperty.getValue().size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = Short.parseShort(parsedProperty.getValue().get(i));
            }
            args.add(array);
        } else if (MapperUtils.isEnum(argClass)) {
            Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) argClass;
            args.add(parsedProperty.getValue().stream()
                            .map(p -> Arrays.stream(enumType.getEnumConstants())
                                    .filter(e -> e.name().equals(p))
                                    .findFirst()
                                    .orElseThrow())
                                    .toArray(size -> (Enum<?>[]) Array.newInstance(enumType, size)));
            }
    }

    @SuppressWarnings("unchecked")
    private static void processSingleSimpleArg(List<Object> args, Class<?> argClass, ParsedProperty parsedProperty) {
        if (String.class.equals(argClass)) {
            args.add(parsedProperty.getValue().getFirst());
        } else if (MapperUtils.isInt(argClass)) {
            args.add(Integer.valueOf(parsedProperty.getValue().getFirst()));
        } else if (MapperUtils.isLong(argClass)) {
            args.add(Long.valueOf(parsedProperty.getValue().getFirst()));
        } else if (MapperUtils.isDouble(argClass)) {
            args.add(Double.valueOf(parsedProperty.getValue().getFirst()));
        } else if (MapperUtils.isBoolean(argClass)) {
            args.add(Boolean.valueOf(parsedProperty.getValue().getFirst()));
        } else if (MapperUtils.isCharacter(argClass)) {
            args.add(parsedProperty.getValue().getFirst().charAt(0));
        } else if (MapperUtils.isFloat(argClass)) {
            args.add(Float.valueOf(parsedProperty.getValue().getFirst()));
        } else if (MapperUtils.isByte(argClass)) {
            args.add(Byte.valueOf(parsedProperty.getValue().getFirst()));
        } else if (MapperUtils.isShort(argClass)) {
            args.add(Short.valueOf(parsedProperty.getValue().getFirst()));
        } else if (MapperUtils.isEnum(argClass)) {
            Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) argClass;
            args.add(Arrays.stream(enumType.getEnumConstants())
                    .filter(e -> e.name().equals(parsedProperty.getValue().getFirst()))
                    .findFirst()
                    .orElse(null));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processNestedArg(List<Object> args, ParsedProperty parsedProperty, Class<?> argClass) {
        List subParsedMapped = new LinkedList<>();
        for (Map<String, ParsedProperty> subParsed : parsedProperty.getNestedProperty()) {
            try {
                subParsedMapped.add(this.fromJson(subParsed, argClass));
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        final Class<?> subClass = argClass;

        if (subParsedMapped.size() == 1) {
            args.add(subParsedMapped.getFirst());
        } else {
            args.add(subParsedMapped.toArray(size -> Array.newInstance(subClass, size)));
        }
    }

    private void writeValueProperty(StringBuilder builder, Object fieldArray, int j) {
        builder.append("\"").append(Array.get(fieldArray, j)).append("\"");
    }

    private boolean hasMoreElements(int j, int fieldArrayLength) {
        return j + 1 < fieldArrayLength;
    }

    private Object getFieldArray(Object object, Class<?> clazz, String fieldName) {
        return MapperUtils.sneakyInvoke(clazz, fieldName, object);
    }

    private Class<?> getFieldClass(Field field) {
        return field.getType().getComponentType();
    }

    private void writeArrayBeginChar(StringBuilder builder) {
        builder.append("[");
    }

    private boolean filedIsArray(Field field) {
        return field.getType().isArray();
    }

    private void writeKeyPropertyWithColon(StringBuilder builder, String fieldName) {
        builder.append("\"").append(fieldName).append("\"").append(":");
    }

    private boolean hasMoreFields(int i, Field[] declaredFields) {
        return i + 1 < declaredFields.length;
    }

    public <T> T fromJson(String json, Class<T> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var parsed = jsonToPropertyMapper.mapJson2Map(json);
        return fromJson(parsed, clazz);
    }

    private void endJson(StringBuilder builder) {
        builder.append("}");
    }

    private void writeFieldAsSingleValue(Object object, Field field, StringBuilder builder, Class<?> fieldClass, String fieldName) {
        Object fieldValue = MapperUtils.sneakyInvoke(fieldClass, fieldName, object);
        if (MapperUtils.isPrimitiveOrString(field.getType())) {
            builder.append("\"").append(fieldValue).append("\"");
        } else {
            builder.append(toJson(fieldValue));
        }
    }

    private void writeFieldAsArray(Object object, StringBuilder builder, Field field, Class<?> clazz, String fieldName) {
        writeArrayBeginChar(builder);
        var arrayClass = getFieldClass(field);
        Object fieldArray = getFieldArray(object, clazz, fieldName);
        int fieldArrayLength = Array.getLength(fieldArray);
        if (arrayClass.isPrimitive()) {
            for (int j = 0; j < fieldArrayLength; j++) {
                writeValueProperty(builder, fieldArray, j);
                if (hasMoreElements(j, fieldArrayLength)) {
                    builder.append(",");
                }
            }
        } else {
            for (int j = 0; j < fieldArrayLength; j++) {
                builder.append(this.toJson(Array.get(fieldArray, j)));
                if (hasMoreElements(j, fieldArrayLength)) {
                    builder.append(",");
                }
            }
        }
        builder.append("]");
    }

    record Pair<L, R> (L left, R right) {}
}
