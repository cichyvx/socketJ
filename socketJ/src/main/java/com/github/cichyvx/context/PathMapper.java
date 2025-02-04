package com.github.cichyvx.context;

import com.github.cichyvx.controller.Controller;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PathMapper {

    private final Map<String, Map<String, Controller<?>>> controllersMap;

    PathMapper(List<Controller<?>> controllerList) {
        Map<String, Map<String, Controller<?>>> controllersMap = new HashMap<>();
        for (Controller<?> controller : controllerList) {
            var x = controllersMap.get(controller.getPath());

            if (x == null) {
                var newMap = new HashMap<String, Controller<?>>();
                newMap.put(controller.getMethod(), controller);
                controllersMap.put(controller.getPath(), newMap);
            } else {
                x.put(controller.getMethod(), controller);
            }
        }
        this.controllersMap = controllersMap;
    }

    public Controller<?> getController(String path, String method) {
        var methods = controllersMap.get(path);
        return methods != null ? methods.get(method) : null;
    }
}
