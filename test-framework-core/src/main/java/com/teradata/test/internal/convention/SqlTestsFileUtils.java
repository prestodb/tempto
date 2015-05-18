/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public final class SqlTestsFileUtils
{
    private SqlTestsFileUtils()
    {
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

    public static String getFilenameWithoutExtension(Path path)
    {
        return removeExtension(path.getFileName().toString());
    }

    public static String getExtension(Path path)
    {
        return FilenameUtils.getExtension(path.getFileName().toString());
    }

    public static void makeExecutable(Path path)
    {
        checkState(path.toFile().setExecutable(true), "Could not make: " + path.toAbsolutePath().toString() + " executable");
    }

    public static void copyRecursive(Path source, Path target)
    {
        try {
            Files.walk(source).forEach(copyFileRecursive(source, target));
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Consumer<Path> copyFileRecursive(Path source, Path target)
    {
        return (Path file) -> {
            try {
                Files.copy(file, target.resolve(source.relativize(file).toString()), COPY_ATTRIBUTES);
            }
            catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        };
    }
}
