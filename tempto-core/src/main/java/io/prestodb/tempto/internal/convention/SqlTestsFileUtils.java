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

package io.prestodb.tempto.internal.convention;

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
                Path targetPath = target.resolve(source.relativize(file).toString());
                if (Files.exists(targetPath) && Files.isDirectory(targetPath)) {
                    return;
                }
                Files.copy(file, targetPath, COPY_ATTRIBUTES);
            }
            catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        };
    }
}
