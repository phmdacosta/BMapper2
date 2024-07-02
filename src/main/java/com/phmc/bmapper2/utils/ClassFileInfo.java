package com.phmc.bmapper2.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@AllArgsConstructor
@Getter
public class ClassFileInfo {
    private final String name;
    private final String sourceName;
    private final String compileName;
    private final String sourcePath;
    private final String compiledPath;
    private final File sourceFile;
    private final File compiledFile;
    private final String module;
    private final Package _package;
    private final Class<?> _class;
}
