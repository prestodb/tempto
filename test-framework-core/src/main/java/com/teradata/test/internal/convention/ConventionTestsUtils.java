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
import java.util.function.Consumer;

import static com.teradata.test.internal.convention.SqlTestsFileUtils.copyRecursive;
import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.FileSystems.newFileSystem;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;

public final class ConventionTestsUtils
{
    private static final String CONVENTION_TESTS_ROOT_DIR = "sql-tests";
    private static final String FILE_SCHEME = "file";
    private static final String JAR_SCHEME = "jar";
    private static final String JAR_FILE_PREFIX = "!";

    private static Optional<Path> temporaryTestsRootPath = Optional.empty();

    public static Optional<Path> getConventionsTestsPath(String child)
    {
        try {
            URL productTestUrl = getSystemResource(CONVENTION_TESTS_ROOT_DIR + "/" + child);
            if (productTestUrl != null) {
                return Optional.of(copyTestsToTemporaryDirectory(productTestUrl.toURI(), child));
            }
            else {
                return Optional.empty();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Path copyTestsToTemporaryDirectory(URI productTestsUri, String child)
            throws IOException
    {
        ensureTemporaryTestsRootPathExists();

        Path temporaryTestsPath = temporaryTestsRootPath.get().resolve(child);
        if (!exists(temporaryTestsPath)) {
            processPathFromUri(productTestsUri, (Path path) -> copyRecursive(path, temporaryTestsPath));
        }

        return temporaryTestsPath;
    }

    private static void ensureTemporaryTestsRootPathExists()
            throws IOException
    {
        if (!temporaryTestsRootPath.isPresent()) {
            temporaryTestsRootPath = Optional.of(createTempDirectory("product_tests"));
            temporaryTestsRootPath.get().toFile().deleteOnExit();
        }
    }

    /**
     * Converts given {@link URI} to {@link Path} and process it using
     * given {@link Consumer}. If the URI points to a JAR file it opens
     * a new {@link FileSystem} for the duration of processing.
     * <p>
     * Example URIs: jar:/foo/bar.jar!/bar/foo; file://foo/bar
     */
    private static void processPathFromUri(URI uri, Consumer<Path> action)
    {
        String scheme = uri.getScheme();
        if (scheme.equals(FILE_SCHEME)) {
            action.accept(Paths.get(uri));
        }

        if (!scheme.equals(JAR_SCHEME)) {
            throw new IllegalArgumentException("URI scheme not supported: " + uri);
        }

        String s = uri.toString();
        int separator = s.indexOf(JAR_FILE_PREFIX);
        String entryName = s.substring(separator + JAR_FILE_PREFIX.length());
        URI fileUri = URI.create(s.substring(0, separator));

        try (FileSystem fileSystem = newFileSystem(fileUri, Maps.newHashMap())) {
            action.accept(fileSystem.getPath(entryName));
        }
        catch (IOException e) {
            throw new RuntimeException("Could not process URI: " + uri, e);
        }
    }

    private ConventionTestsUtils()
    {
    }
}
