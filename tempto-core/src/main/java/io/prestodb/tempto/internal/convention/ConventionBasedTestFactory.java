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

import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.internal.convention.generator.GeneratorPathTestFactory;
import io.prestodb.tempto.internal.convention.recursion.RecursionPathTestFactory;
import io.prestodb.tempto.internal.convention.sql.SqlPathTestFactory;
import org.slf4j.Logger;
import org.testng.annotations.Factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static io.prestodb.tempto.fulfillment.table.TableDefinitionsRepository.tableDefinitionsRepository;
import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.testConfiguration;
import static io.prestodb.tempto.internal.convention.ConventionTestsUtils.getConventionsTestsPath;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class ConventionBasedTestFactory
{
    private static final Logger LOGGER = getLogger(ConventionBasedTestFactory.class);
    private static final ConventionBasedTest[] NO_TEST_CASES = new ConventionBasedTest[0];

    // TODO: make this configurable
    private static final String TEST_PACKAGE = "io.prestodb.tempto";
    public static final String TESTCASES_PATH_PART = "testcases";

    public interface PathTestFactory
    {
        boolean isSupportedPath(Path path);

        List<ConventionBasedTest> createTestsForPath(Path path, String testNamePrefix, ConventionBasedTestFactory factory);
    }

    private List<PathTestFactory> factories;

    @Factory
    public Object[] createTestCases()
    {
        LOGGER.debug("Loading file based test cases");
        try {
            Optional<Path> productTestsPath = getConventionsTestsPath(TESTCASES_PATH_PART);
            if (!productTestsPath.isPresent()) {
                LOGGER.info("No convention tests cases");
                return NO_TEST_CASES;
            }

            factories = setupFactories();
            return createTestsForRootPath(productTestsPath.get());
        }
        catch (Exception e) {
            LOGGER.error("Could not create file test", e);
            throw new RuntimeException("Could not create test cases", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<PathTestFactory> setupFactories()
    {
        Configuration configuration = testConfiguration();
        return ImmutableList.of(
                new RecursionPathTestFactory(),
                new GeneratorPathTestFactory(),
                new SqlPathTestFactory(
                        tableDefinitionsRepository(),
                        new ConventionBasedTestProxyGenerator(TEST_PACKAGE), configuration));
    }

    public List<ConventionBasedTest> createTestsForPath(Path path, String testNamePrefix)
    {
        return factories.stream()
                .filter(f -> f.isSupportedPath(path))
                .flatMap(f -> f.createTestsForPath(path, testNamePrefix, this).stream())
                .collect(toList());
    }

    public List<ConventionBasedTest> createTestsForChildrenOfPath(Path path, String testNamePrefix)
    {
        try {
            // TODO tree traversal for ZIP file system (when resources are inside jar) results with Exception
            return Files.list(path)
                    .flatMap(child -> createTestsForPath(child, testNamePrefix).stream())
                    .collect(toList());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ConventionBasedTest[] createTestsForRootPath(Path path)
    {
        return createTestsForPath(path, "sql_tests").toArray(new ConventionBasedTest[0]);
    }
}
