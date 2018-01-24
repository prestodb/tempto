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

package io.prestodb.tempto.internal.convention.generator;

import io.prestodb.tempto.internal.convention.ConventionBasedTest;
import io.prestodb.tempto.internal.convention.ConventionBasedTestFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.io.Files.createTempDir;
import static io.prestodb.tempto.internal.convention.ProcessUtils.execute;
import static io.prestodb.tempto.internal.convention.SqlTestsFileUtils.getExtension;
import static io.prestodb.tempto.internal.convention.SqlTestsFileUtils.getFilenameWithoutExtension;
import static io.prestodb.tempto.internal.convention.SqlTestsFileUtils.makeExecutable;

public class GeneratorPathTestFactory
        implements ConventionBasedTestFactory.PathTestFactory
{
    private static final String GENERATOR_SCRIPT_EXTENSION = "generator";

    @Override
    public boolean isSupportedPath(Path path)
    {
        return GENERATOR_SCRIPT_EXTENSION.equals(getExtension(path));
    }

    @Override
    public List<ConventionBasedTest> createTestsForPath(Path path, String testNamePrefix, ConventionBasedTestFactory factory)
    {
        String newPrefix = testNamePrefix + "." + getFilenameWithoutExtension(path);
        File tmpTestsDirectory = createTempDir();
        tmpTestsDirectory.deleteOnExit();
        makeExecutable(path);
        execute(path.toFile().toString(), tmpTestsDirectory.toString());
        return factory.createTestsForChildrenOfPath(tmpTestsDirectory.toPath(), newPrefix);
    }
}
