package com.example.agent;

import javassist.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

public class ClassTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.endsWith("HttpBackendService")) {
            System.out.println("Instrumenting " + className);
            try {
                ClassPool classPool = ClassPool.getDefault();
                CtClass clazz = classPool.get("com.frontend.to.backend.HttpBackendService"); // TODO get from input params
                addTiming(clazz, "chatAsync");

                byte[] byteCode = clazz.toBytecode();
                clazz.detach();

                store(byteCode, "C:/Work/" + clazz.getSimpleName() + ".class");
                return byteCode;
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
        }

        return classfileBuffer;
    }

    private static void addTiming(CtClass clazz, String methodName) throws NotFoundException, CannotCompileException {
        CtMethod oldMethod = clazz.getDeclaredMethod(methodName);

        // Rename old method to synthetic name
        String changedName = methodName + "$orig";
        oldMethod.setName(changedName);

        // Duplicate the method with original name for use as interceptor
        CtMethod newMethod = CtNewMethod.copy(oldMethod, methodName, clazz, null);

        String type = oldMethod.getReturnType().getName();

        StringBuffer body = new StringBuffer();
        body.append("{ long start = System.currentTimeMillis();");

        if (!"void".equals(type)) {
            body.append(type + " result = "); // Capture returned value (if not void)
        }

        body.append(changedName + "($$);"); // Call existed method

        //  Print timing information
        body.append("System.out.println(\"" + methodName + " took \" + (System.currentTimeMillis()-start) + \" ms.\");");

        if (!"void".equals(type)) {
            body.append("return result;"); //  Return captured value (if not void)
        }

        body.append("}");

        newMethod.setBody(body.toString());
        clazz.addMethod(newMethod);
    }

    private static void store(byte[] byteCode, String path) throws IOException {
        FileOutputStream os = new FileOutputStream(path);
        os.write(byteCode);
        os.close();
    }
}