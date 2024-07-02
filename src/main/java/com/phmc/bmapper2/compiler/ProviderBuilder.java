package com.phmc.bmapper2.compiler;

public class ProviderBuilder {
    public static IProvider build() {
        Class<? extends IProvider> clazz = ToolProvider.class;
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            System.out.printf("Could not instantiate %s%n", clazz.getSimpleName());
        }
        return null;
    }
}
