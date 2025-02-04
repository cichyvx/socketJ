package com.github.cichyvx.controller;

import com.github.cichyvx.http.HttpRequest;
import com.github.cichyvx.mapper.ObjectMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ControllerInvoker {

    public static final String CONTROLLER_PROCESS_METHOD_NAME = "process";
    private final ObjectMapper objectMapper;
    private final static Map<Controller<?>, Method> cached = new ConcurrentHashMap<>();

    public ControllerInvoker(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Response<?> invokeController(Controller<?> controller, HttpRequest httpRequest) {
        Type type = ControllerUtils.getControllerInterfaceFrom(controller);
        if (type instanceof ParameterizedType parameterizedType) {
            var RequestClazz = ControllerUtils.getGenericClassFromType(parameterizedType);
            Method method;
            if (!cached.containsKey(controller)) {
                method = getMethod(controller);
                cached.putIfAbsent(controller, method);
            } else {
                method = cached.get(controller);
            }
            return (Response<?>) getInvoked(controller, httpRequest, method, RequestClazz);
        } else {
            throw new IllegalStateException("Unsupported type: " + type);
        }

    }

    private Object getInvoked(Controller<?> controller, HttpRequest httpRequest, Method method,
                             Class<? extends Request> RequestClazz) {
        try {
            return method.invoke(controller, mapRequestToObj(httpRequest, RequestClazz));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T mapRequestToObj(HttpRequest httpRequest, Class<T> RequestClazz) {
            try {
                return objectMapper.fromJson(httpRequest.body(), RequestClazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    private static Method getMethod(Controller<?> controller) {
        try {
            return controller.getClass().getMethod(CONTROLLER_PROCESS_METHOD_NAME, Request.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
