package com.phmc.bmapper2.compiler;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import java.util.Date;

public class ToolProvider implements IProvider {
    @Override
    public JavaCompiler getJavaCompiler() {
        return javax.tools.ToolProvider.getSystemJavaCompiler();
    }

    @Override
    public void checkVersions() {
        long start = (new Date()).getTime();;
        JavaCompiler compiler = this.getJavaCompiler();
        for (SourceVersion version : compiler.getSourceVersions()) {
            System.out.println(version);
        }
        long end = (new Date()).getTime();
        System.out.println(end - start);
    }
}
