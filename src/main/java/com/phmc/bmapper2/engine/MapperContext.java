package com.phmc.bmapper2.engine;

import java.util.*;

public class MapperContext {
    private final String MAP_PROP_SET_KEY = "MAP_PROP_SET_";
    private final String MAP_METHOD_SET_KEY = "MAP_METH_SET_";
    private final Map<String, Object> ctx;

    public MapperContext() {
        ctx = new HashMap<>();
    }

    public Set<MappingPair> getPropertyPairs(Class<?> fromClass, Class<?> toClass) {
        return (Set<MappingPair>) ctx.get(buildMappingKey(MAP_PROP_SET_KEY, fromClass, toClass));
    }

    public Set<MappingPair> getMethodPairs(Class<?> fromClass, Class<?> toClass) {
        return (Set<MappingPair>) ctx.get(buildMappingKey(MAP_METHOD_SET_KEY, fromClass, toClass));
    }

    public void putPropertyPair(Class<?> fromClass, Class<?> toClass, String from, String to) {
        MappingPair pair = new MappingPair(from, to);
        String key = buildMappingKey(MAP_PROP_SET_KEY, fromClass, toClass);
        Set<MappingPair> set = (Set<MappingPair>) ctx.get(key);
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(pair);
        ctx.put(key, set);
        putMethodPair(fromClass, toClass, from, to);
    }

    public void putMethodPair(Class<?> fromClass, Class<?> toClass, String from, String to) {
        String fromMethod = from;
        if (isNotMethod(fromClass, from)) {
            fromMethod = buildGetterName(from);
        }
        String toMethod = to;
        if (isNotMethod(toClass, to)) {
            toMethod = buildSetterName(to);
        }
        MappingPair pair = new MappingPair(fromMethod, toMethod);
        String key = buildMappingKey(MAP_METHOD_SET_KEY, fromClass, toClass);
        Set<MappingPair> set = (Set<MappingPair>) ctx.get(key);
        if (set == null) {
            set = new HashSet<>();
        }
        set.add(pair);
        ctx.put(key, set);
    }

    private String buildMappingKey(String prefix, Class<?> fromClass, Class<?> toClass) {
        return prefix + fromClass.getSimpleName() + "_" + toClass.getSimpleName();
    }

    private String buildSetterName(String propName) {
        return buildMethodName("set", propName);
    }

    private String buildGetterName(String propName) {
        return buildMethodName("get", propName);
    }

    private String buildMethodName(String prefix, String propName) {
        return prefix + propName.substring(0, 1).toUpperCase() + propName.substring(1);
    }

    private boolean isNotMethod(Class<?> clazz, String name) {
        return Arrays.stream(clazz.getMethods()).noneMatch(m -> m.getName().equals(name));
    }
}
