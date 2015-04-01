/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;

import static org.apache.commons.io.FilenameUtils.removeExtension;

public final class SqlTestsFileUtils
{
    private SqlTestsFileUtils()
    {
    }

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

    public static String getFilenameWithoutExtension(Path path)
    {
        return removeExtension(path.getFileName().toString());
    }

    public static String getFilenameWithoutExtension(File file)
    {
        return removeExtension(file.getName());
    }

    public static String getExtension(Path path)
    {
        return FilenameUtils.getExtension(path.getFileName().toString());
    }

    public static String getExtension(File file)
    {
        return FilenameUtils.getExtension(file.getName());
    }
}
