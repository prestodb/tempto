/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.FileSystems.newFileSystem;

public final class ConventionTestsUtils
{
    private static final String CONVENTION_TESTS_ROOT_DIR = "sql-tests";
    private static final String FILE_SCHEME = "file";
    private static final String JAR_SCHEME = "jar";
    private static final String JAR_FILE_PREFIX = "!/";

    public static Optional<URI> getConventionsTestsUri(String child)
    {
        try {
            URL productTestUrl = getSystemResource(CONVENTION_TESTS_ROOT_DIR + "/" + child);
            if (productTestUrl != null) {
                return Optional.of(productTestUrl.toURI());
            }
            else {
                return Optional.empty();
            }
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Converts given {@link URI} to {@link Path} and process it using
     * given {@link Function}. If the URI points to a JAR file it opens
     * a new {@link FileSystem} for the duration of processing.
     * <p>
     * Example URIs: jar:/foo/bar.jar!/bar/foo; file://foo/bar
     */
    public static <T> T processPathFromUri(URI uri, Function<Path, T> function)
    {
        String scheme = uri.getScheme();
        if (scheme.equals(FILE_SCHEME)) {
            return function.apply(Paths.get(uri));
        }

        if (!scheme.equals(JAR_SCHEME)) {
            throw new IllegalArgumentException("URI scheme not supported: " + uri);
        }

        String s = uri.toString();
        int separator = s.indexOf(JAR_FILE_PREFIX);
        String entryName = s.substring(separator + JAR_FILE_PREFIX.length());
        URI fileUri = URI.create(s.substring(0, separator));

        try (FileSystem fileSystem = newFileSystem(fileUri, Maps.newHashMap())) {
            return function.apply(fileSystem.getPath(entryName));
        }
        catch (IOException e) {
            throw new RuntimeException("Could not process URI: " + uri, e);
        }
    }

    private ConventionTestsUtils()
    {
    }
}
