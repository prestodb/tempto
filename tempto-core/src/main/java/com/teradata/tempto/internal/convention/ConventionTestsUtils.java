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

package com.teradata.tempto.internal.convention;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.teradata.tempto.internal.convention.SqlTestsFileUtils.copyRecursive;
import static java.lang.ClassLoader.getSystemResources;
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
            Enumeration<URL> productTestUrls = getSystemResources(CONVENTION_TESTS_ROOT_DIR + "/" + child);
            List<URI> productTestUris = new ArrayList<>();
            while (productTestUrls.hasMoreElements()) {
                URL url = productTestUrls.nextElement();
                productTestUris.add(url.toURI());
            }

            if (!productTestUris.isEmpty()) {
                return Optional.of(copyTestsToTemporaryDirectory(productTestUris, child));
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

    private static Path copyTestsToTemporaryDirectory(List<URI> productTestsUris, String child)
            throws IOException
    {
        ensureTemporaryTestsRootPathExists();

        Path temporaryTestsPath = temporaryTestsRootPath.get().resolve(child);
        if (!exists(temporaryTestsPath)) {
            for (URI uri : productTestsUris) {
                processPathFromUri(uri, (Path path) -> copyRecursive(path, temporaryTestsPath));
            }
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
     * lConverts given {@link URI} to {@link Path} and process it using
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
            return;
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
