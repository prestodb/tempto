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

package io.prestodb.tempto.internal.convention

import io.prestodb.tempto.Requirement
import io.prestodb.tempto.configuration.Configuration
import io.prestodb.tempto.internal.DummyTestRequirement
import io.prestodb.tempto.internal.convention.sql.SqlQueryConventionBasedTest
import org.testng.annotations.Test
import spock.lang.Specification

import java.lang.reflect.Method
import java.nio.file.Path
import java.nio.file.Paths

import static com.google.common.collect.Iterables.getOnlyElement
import static java.util.Collections.emptySet
import static org.assertj.core.api.Assertions.assertThat

class ConventionBasedTestProxyGeneratorTest
        extends Specification
{
    private ConventionBasedTestProxyGenerator proxyGenerator = new ConventionBasedTestProxyGenerator("io.prestodb.tempto");

    def 'testGenerateProxy'()
    {
        when:
        Path testFile = file("convention/sample-test/query1.sql")
        SqlQueryDescriptor queryDescriptor = new SqlQueryDescriptor(section(testFile))
        SqlResultDescriptor resultDescriptor = new SqlResultDescriptor(section(testFile))
        Requirement requirement = Mock(Requirement)
        ConventionBasedTest testInstance = new SqlQueryConventionBasedTest(
                Optional.empty(),
                Optional.empty(),
                testFile,
                "test.prefix",
                1,
                5,
                queryDescriptor,
                resultDescriptor,
                requirement)

        ConventionBasedTest proxiedTest = proxyGenerator.generateProxy(testInstance)
        Class<ConventionBasedTest> proxiedClass = proxiedTest.getClass()
        Method testMethod = proxiedClass.getMethod("query1_1")
        Test testAnnotation = testMethod.getAnnotation(Test)

        then:
        assertThat(proxiedTest.getRequirements()).isSameAs(requirement)
        assertThat(testAnnotation).isNotNull()
        assertThat(testAnnotation.enabled()).isTrue()
        assertThat(testAnnotation.groups()).containsOnly("tpch", "quarantine")
    }

    private file(String path)
    {
        Paths.get(getClass().getClassLoader().getResource(path).getPath())
    }

    private section(Path file)
    {
        getOnlyElement(new AnnotatedFileParser().parseFile(file))
    }

    def 'test class name and method names properly generated'()
    {
        setup:
        def test = DummyConventionBasedTest.emptyTest(testName);
        def proxy = proxyGenerator.generateProxy(test);
        def proxyMethodNames = proxy.getClass().getMethods().collect { it.name }

        expect:
        proxy.getClass().getName() == expectedClassName;
        proxyMethodNames.contains(expectedMethodName);

        where:
        testName                       | expectedClassName                 | expectedMethodName
        'a.b.c.d'                      | 'io.prestodb.tempto.c'            | 'd'
        'a.b.9c.1d'                    | 'io.prestodb.tempto._9c'          | '_1d'
        'a.b.ala ma kota.a-kot-ma ale' | 'io.prestodb.tempto.ala_ma_kota'  | 'a_kot_ma_ale'
    }

    private static class DummyConventionBasedTest
            extends ConventionBasedTest
    {
        private final Requirement requirement
        private final String testName;
        private final Set<String> testGroups;

        DummyConventionBasedTest(Requirement requirement, String testName, Set<String> testGroups)
        {
            this.requirement = requirement
            this.testName = testName
            this.testGroups = testGroups
        }

        @Override
        void test()
        {}

        @Override
        Requirement getRequirements(Configuration configuration)
        {
            return requirement;
        }

        @Override
        String getTestName()
        {
            return testName;
        }

        @Override
        Set<String> getTestGroups()
        {
            return testGroups;
        }

        static DummyConventionBasedTest emptyTest(String testName)
        {
            return new DummyConventionBasedTest(new DummyTestRequirement(), testName, emptySet());
        }
    }
}
