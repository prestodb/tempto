/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestodb.tempto.process;

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
