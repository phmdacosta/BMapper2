package com.phmc.bmapper2.utils;

import java.io.File;

public class ClassUtils {
    private static final String SOURCE_JAVA_PATH = "src/main/java/";

    private ClassUtils() {}

    public static ClassFileInfo getFileInfo(Class<?> clazz) {
        File source = getSourceFile(clazz);
        File compiled = getCompiledFile(clazz);
        String module = getModule(clazz);
        return new ClassFileInfo(
                source.getName(),
                source.getName(),
                compiled.getName(),
                source.getParentFile().getPath(),
                compiled.getParentFile().getPath(),
                source,
                compiled,
                module.substring(0, module.length() - 1),
                clazz.getPackage(),
                clazz);
    }

    public static String getSourcePath(Class<?> clazz) {
        String root = System.getProperty("user.dir") + "/";
        String module = getModule(clazz);
        return root + module + getSourceJavaPath() + getPathPackage(clazz);
    }

    public static String getCompiledPath(Class<?> clazz) {
        String root = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        return root + getPathPackage(clazz);
    }

    public static File getSourceFile(Class<?> clazz) {
        String path = getSourcePath(clazz);
        return new File(path + clazz.getSimpleName() + ".java");
    }

    public static File getCompiledFile(Class<?> clazz) {
        String path = getCompiledPath(clazz);
        return new File(path + clazz.getSimpleName() + ".class");
    }

    private static String getSourceJavaPath() {
        return SOURCE_JAVA_PATH;
    }

    private static String getModule(Class<?> clazz) {
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath()).getName() + "/";
    }

    private static String getPathPackage(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        return "/" + packageName.replace(".", "/") + "/";
    }
}
