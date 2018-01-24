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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.configuration.Configuration;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.Character.isAlphabetic;
import static java.lang.ClassLoader.getSystemClassLoader;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Generates proxy class with proper name for each convention based test. Generated class
 * has name equal to directory grouping test cases. Test case is then mapped to method.
 */
public class ConventionBasedTestProxyGenerator
{
    private static final Logger LOGGER = getLogger(ConventionBasedTestProxyGenerator.class);

    private final String testPackage;

    public ConventionBasedTestProxyGenerator(String testPackage)
    {
        this.testPackage = testPackage;
    }

    public ConventionBasedTest generateProxy(ConventionBasedTest conventionBasedTest)
    {
        try {
            String className = generatedClassName(conventionBasedTest);
            String methodName = generatedMethodName(conventionBasedTest);
            TestAnnotationImpl testAnnotationImpl = new TestAnnotationImpl(conventionBasedTest);

            DynamicType.Unloaded<ConventionBasedTestProxy> dynamicType = new ByteBuddy()
                    .subclass(ConventionBasedTestProxy.class)
                    .name(className)
                    .defineMethod(methodName, void.class, ImmutableList.of(), Visibility.PUBLIC)
                    .intercept(MethodCall.invoke(ConventionBasedTestProxy.class.getMethod("test")))
                    .annotateMethod(testAnnotationImpl)
                    .make();

            LOGGER.debug("Generating proxy class: {}.{}, annotation: {}", className, methodName, testAnnotationImpl);

            return dynamicType
                    .load(getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .getConstructor(ConventionBasedTest.class)
                    .newInstance(conventionBasedTest);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException("Could not create proxy for convention test: " + conventionBasedTest, e);
        }
    }

    private String generatedClassName(ConventionBasedTest conventionBasedTest)
    {
        List<String> testNameParts = Splitter.on('.').splitToList(conventionBasedTest.getTestName());
        return testPackage + "." + toJavaSymbol(testNameParts.get(testNameParts.size() - 2));
    }

    private String generatedMethodName(ConventionBasedTest conventionBasedTest)
    {
        List<String> testNameParts = Splitter.on('.').splitToList(conventionBasedTest.getTestName());
        return toJavaSymbol(Iterables.getLast(testNameParts));
    }

    private String toJavaSymbol(String s)
    {
        if (s.isEmpty()) {
            return s;
        }
        String javaSymbol = s.replaceAll("[^A-Za-z0-9_]", "_");
        if (!isAlphabetic(javaSymbol.charAt(0))) {
            javaSymbol = "_" + javaSymbol;
        }
        return javaSymbol;
    }

    public static class ConventionBasedTestProxy
            extends ConventionBasedTest
    {
        private final ConventionBasedTest delegate;

        public ConventionBasedTestProxy(ConventionBasedTest delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public void test()
        {
            delegate.test();
        }

        @Override
        public String getTestName()
        {
            return delegate.getTestName();
        }

        @Override
        public Set<String> getTestGroups()
        {
            return delegate.getTestGroups();
        }

        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return delegate.getRequirements(configuration);
        }
    }

    private static class TestAnnotationImpl
            implements Test
    {
        private final ConventionBasedTest conventionBasedTest;

        public TestAnnotationImpl(ConventionBasedTest conventionBasedTest)
        {
            this.conventionBasedTest = conventionBasedTest;
        }

        @Override
        public String[] groups()
        {
            Set<String> testGroups = conventionBasedTest.getTestGroups();
            return testGroups.toArray(new String[testGroups.size()]);
        }

        @Override
        public boolean enabled()
        {
            return true;
        }

        @Override
        public String[] parameters()
        {
            return new String[0];
        }

        @Override
        public String[] dependsOnGroups()
        {
            return new String[0];
        }

        @Override
        public String[] dependsOnMethods()
        {
            return new String[0];
        }

        @Override
        public long timeOut()
        {
            return 0;
        }

        @Override
        public long invocationTimeOut()
        {
            return 0;
        }

        @Override
        public int invocationCount()
        {
            return 1;
        }

        @Override
        public int threadPoolSize()
        {
            return 0;
        }

        @Override
        public int successPercentage()
        {
            return 100;
        }

        @Override
        public String dataProvider()
        {
            return "";
        }

        @Override
        public Class<?> dataProviderClass()
        {
            return Object.class;
        }

        @Override
        public boolean alwaysRun()
        {
            return false;
        }

        @Override
        public String description()
        {
            return "";
        }

        @Override
        public Class[] expectedExceptions()
        {
            return new Class[0];
        }

        @Override
        public String expectedExceptionsMessageRegExp()
        {
            return "";
        }

        @Override
        public String suiteName()
        {
            return "";
        }

        @Override
        public String testName()
        {
            return "";
        }

        @Override
        public boolean sequential()
        {
            return false;
        }

        @Override
        public boolean singleThreaded()
        {
            return false;
        }

        @Override
        public Class retryAnalyzer()
        {
            return Class.class;
        }

        @Override
        public boolean skipFailedInvocations()
        {
            return false;
        }

        @Override
        public boolean ignoreMissingDependencies()
        {
            return false;
        }

        @Override
        public int priority()
        {
            return 0;
        }

        @Override
        public Class<? extends Annotation> annotationType()
        {
            return Test.class;
        }

        @Override
        public String toString()
        {
            return toStringHelper(this)
                    .add("groups", Arrays.toString(groups()))
                    .toString();
        }
    }
}
