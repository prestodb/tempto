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

package io.prestodb.tempto.internal.convention.recursion;

import io.prestodb.tempto.internal.convention.ConventionBasedTest;
import io.prestodb.tempto.internal.convention.ConventionBasedTestFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RecursionPathTestFactory
        implements ConventionBasedTestFactory.PathTestFactory
{
    @Override
    public boolean isSupportedPath(Path path)
    {
        return Files.isDirectory(path);
    }

    @Override
    public List<ConventionBasedTest> createTestsForPath(Path path, String testNamePrefix, ConventionBasedTestFactory factory)
    {
        String newPrefix = testNamePrefix + "." + path.getFileName();
        return factory.createTestsForChildrenOfPath(path, newPrefix);
    }
}
