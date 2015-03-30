/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public final class SqlTestsFileUtils
{

    public static File changeExtension(File source, String extension)
    {
        String newFileName = changeExtension(extension, source.getName());
        return new File(source.getParent(), newFileName);
    }

    public static Path changeExtension(Path source, String extension)
    {
        String newFileName = changeExtension(extension, source.getFileName().toString());
        return source.getParent().resolve(newFileName);
    }

    public static String changeExtension(String extension, String fileName)
    {
        return fileName.substring(0, fileName.lastIndexOf(".")) + '.' + extension;
    }

    public static String trimExtension(String fileName)
    {
        return FilenameUtils.removeExtension(fileName);
    }

    public static ExtensionFileCollectorVisitor extensionFileCollectorVisitor(String... extensions)
    {
        return extensionFileCollectorVisitor(asList(extensions));
    }

    public static ExtensionFileCollectorVisitor extensionFileCollectorVisitor(List<String> extensions)
    {
        return new ExtensionFileCollectorVisitor(extensions);
    }

    private SqlTestsFileUtils()
    {
    }

    public static final class ExtensionFileCollectorVisitor
            extends SimpleFileVisitor<Path>
    {

        private final List<Path> result = newArrayList();
        private final List<String> extensions;

        private ExtensionFileCollectorVisitor(List<String> extensions)
        {
            this.extensions = extensions.stream().map((String extension) -> "." + extension).collect(toList());
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException
        {
            if (any(extensions, (String extension) -> file.getFileName().toString().endsWith(extension))) {
                result.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getResult()
        {
            return result;
        }
    }
}
