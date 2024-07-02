package com.phmc.bmapper2.engine;

import com.phmc.bmapper2.IMapper;
import com.phmc.bmapper2.compiler.IProvider;
import com.phmc.bmapper2.compiler.ProviderBuilder;
import com.phmc.bmapper2.utils.ClassFileInfo;
import com.phmc.bmapper2.utils.ClassUtils;
import com.sun.tools.javac.resources.compiler;
import com.sun.tools.javap.JavapTask;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;

@AllArgsConstructor
public class MapperBuilder {
    private final String SPACE = " ";
    private final String BREAK_LINE = "\n";
    private final String IMPORT = "import";
    private final String PACKAGE = "package";
    private final String END_LINE = ";\n";
    private final String OPEN_QUOTE = " {\n";
    private final String CLOSE_QUOTE = "}\n";

    private MapperContext context;
    private String packagePath;
    private Class<?> fromClass;
    private Class<?> toClass;
    private Class<?> mapperClass;

    private String getImplementationClassName() {
        return mapperClass.getSimpleName() + "Impl";
    }

    public void build() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        IProvider provider = ProviderBuilder.build();
        JavaCompiler compiler = provider.getJavaCompiler();
        DiagnosticCollector<JavaFileObject> ds = new DiagnosticCollector<>();
//        try (StandardJavaFileManager mgr = compiler.getStandardFileManager(ds, null, null)) {
//            ClassFileInfo fileInfo = ClassUtils.getFileInfo(ServiceLoaderWay.class);
//            Iterable<? extends JavaFileObject> sources = mgr.getJavaFileObjectsFromFiles(List.of(fileInfo.getSourceFile()));
//            JavaCompiler.CompilationTask task = compiler.getTask(null, mgr, ds, null, null, sources);
//            task.call();
//        }

        for (Diagnostic<? extends JavaFileObject> d : ds.getDiagnostics()) {
            System.out.format("Line: %d, %s in %s", d.getLineNumber(), d.getMessage(null), d.getSource().getName());
        }

        String classContent = buildCode();
        ClassFileInfo info = ClassUtils.getFileInfo(mapperClass);
        File newSourceFile = new File(info.getSourcePath(), getImplementationClassName() + ".java");
        if (!Files.exists(newSourceFile.getParentFile().toPath())) {
            Files.createDirectory(newSourceFile.getParentFile().toPath());
        }
        Files.write(newSourceFile.toPath(), classContent.getBytes(StandardCharsets.UTF_8));
        String modulePath = info.getCompiledPath().split(info.getModule())[0] + info.getModule() + "/";

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compUnits =  fileManager.getJavaFileObjects(newSourceFile);
        final Iterable<String> options = Arrays.asList("-d", modulePath);
        Boolean compRes = compiler.getTask(null, fileManager, null, options, null, compUnits).call();

        if(compRes){
            System.out.println("Compilation has succeeded");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> compiledClass = cl.loadClass(mapperClass.getPackage().getName() + "." + getImplementationClassName());

            IMapper instance = (IMapper) compiledClass.getDeclaredConstructor().newInstance();
            System.out.println(instance);
        }else {
            System.out.println("Compilation error");
            fileManager.close();
        }
    }

    public String buildCode() {
        Method mapMethod = IMapper.class.getMethods()[0];
        if (mapMethod == null) {
            return null;
        }

        Set<MappingPair> methodPairSet = context.getMethodPairs(fromClass, toClass);

        StringBuilder sb = new StringBuilder();
        // PACKAGE
        sb.append(PACKAGE).append(SPACE).append(packagePath).append(END_LINE);

        // IMPORTS
        buildImports(sb, fromClass);
        buildImports(sb, toClass);
        buildImports(sb, mapperClass);
//        sb.append(IMPORT).append(SPACE).append(fromClass.getName()).append(END_LINE);
//        sb.append(IMPORT).append(SPACE).append(toClass.getName()).append(END_LINE);
//        sb.append(IMPORT).append(SPACE).append(mapperClass.getName()).append(END_LINE);

        // CLASS DECLARATION
        buildClassDeclaration(sb, getImplementationClassName(), null, mapperClass.getSimpleName());
//        sb.append("public class ").append(mapperClass.getSimpleName()).append("Impl").append(" implements ").append(mapperClass.getSimpleName());
//        sb.append(OPEN_QUOTE);

        // METHOD DECLARATION
        String[] paramTypes = {fromClass.getSimpleName(), "Class<? extends" + toClass.getSimpleName() + ">"};
        String[] paramNames = {"from", "toClass"};
        buildOverrideMethodDeclaration(sb, "public", toClass.getSimpleName(), mapMethod.getName(), paramTypes, paramNames);
//        sb.append("@Override");
//        sb.append("public ").append(toClass.getSimpleName()).append(SPACE).append(mapMethod.getName()).append("(").append(fromClass.getSimpleName()).append("from, Class<? extends").append(toClass.getSimpleName()).append(">").append("toClass)");
//        sb.append(OPEN_QUOTE);
        // METHOD IMPLEMENTING
        sb.append(toClass.getSimpleName()).append(" to = new ").append(toClass.getSimpleName()).append("()").append(END_LINE);
        for (MappingPair pair : methodPairSet) {
            sb.append("to.").append(pair.getTo()).append("(from.").append(pair.getFrom()).append("())").append(END_LINE);
        }
        sb.append("return to").append(END_LINE);
        // CLOSING METHOD
        sb.append(CLOSE_QUOTE);

        // COLSING CLASS
        sb.append(CLOSE_QUOTE);

        return sb.toString();
    }

    private void buildImports(StringBuilder sb, Class<?> clazz) {
        sb.append(IMPORT).append(SPACE).append(clazz.getName()).append(END_LINE);
    }

    private void buildClassDeclaration(StringBuilder sb, String className, String superName, String interfaceName) {
        sb.append("public class ").append(className);
        if (StringUtils.hasText(superName)) {
            sb.append(" extends ").append(superName);
        }
        if (StringUtils.hasText(interfaceName)) {
            sb.append(" implements ").append(interfaceName);
        }
        sb.append(OPEN_QUOTE);
    }

    private void buildOverrideMethodDeclaration(StringBuilder sb, String access, String returnType, String methodName, String[] paramTypes, String[] paramNames) {
        sb.append("@Override");
        buildMethodDeclaration(sb, access, returnType, methodName, paramTypes, paramNames);
    }

    private void buildMethodDeclaration(StringBuilder sb, String access, String returnType, String methodName, String[] paramTypes, String[] paramNames) {
        sb.append(access).append(SPACE).append(returnType).append(SPACE).append(methodName).append("(");
        if (paramTypes != null) {
            if (paramNames == null || paramTypes.length != paramNames.length) {
                throw new IllegalArgumentException("Parameter types and names has different size.");
            }
            for (int i = 0; i < paramTypes.length; i++) {
                sb.append(paramTypes[0]).append(SPACE).append(paramNames[0]);
                if (i < paramTypes.length - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append(")").append(OPEN_QUOTE);
    }
}
