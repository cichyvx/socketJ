package com.github.cichyvx.controller;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ControllerUtils {

    public static Type getControllerInterfaceFrom(Object object) {
        if (object instanceof Controller<?> controller) {
            return controller.getClass().getGenericInterfaces()[0];
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Request> getGenericClassFromType(ParameterizedType type) {
        return (Class<? extends Request>) type.getActualTypeArguments()[0];
    }

}
