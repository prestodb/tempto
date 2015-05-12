/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.process;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Launches a Java class with main() method as a separate process.
 */
public final class JavaProcessLauncher
{
    private final String javaBin;
    private final String classpath;

    public static JavaProcessLauncher defaultJavaProcessLauncher()
    {
        return new JavaProcessLauncher(
                System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                System.getProperty("java.class.path")
        );
    }

    public JavaProcessLauncher(String javaBin, String classpath)
    {
        this.javaBin = javaBin;
        this.classpath = classpath;
    }

    public Process launch(Class clazz, List<String> arguments)
            throws IOException, InterruptedException
    {
        String className = clazz.getCanonicalName();
        List<String> command = ImmutableList.<String>builder()
                .add(javaBin, "-cp", classpath, className)
                .addAll(arguments)
                .build();
        return new ProcessBuilder(command).start();
    }
}
