package com.phmc.bmapper2.compiler;

import javax.tools.JavaCompiler;

public interface IProvider {
    JavaCompiler getJavaCompiler();
    void checkVersions();
}
