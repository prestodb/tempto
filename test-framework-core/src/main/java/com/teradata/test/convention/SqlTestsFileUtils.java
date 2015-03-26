/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.convention;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

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

    public static ExtensionFileCollectorVisitor extensionFileCollectorVisitor(String extension)
    {
        return new ExtensionFileCollectorVisitor(extension);
    }

    private SqlTestsFileUtils()
    {
    }

    public static final class ExtensionFileCollectorVisitor
            extends SimpleFileVisitor<Path>
    {

        private final List<Path> result = newArrayList();
        private final String extension;

        private ExtensionFileCollectorVisitor(String extension)
        {
            this.extension = "." + extension;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException
        {
            if (file.getFileName().toString().endsWith(extension)) {
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
