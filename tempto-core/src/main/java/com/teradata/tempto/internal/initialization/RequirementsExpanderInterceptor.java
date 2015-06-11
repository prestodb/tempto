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

package com.teradata.tempto.internal.initialization;

import com.google.common.collect.Lists;
import com.teradata.tempto.Requirement;
import com.teradata.tempto.internal.TestSpecificRequirementsResolver;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.MethodInstance;
import org.testng.internal.Parameters;
import org.testng.internal.TestNGMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.teradata.tempto.internal.configuration.TestConfigurationFactory.createTestConfiguration;
import static java.util.Arrays.asList;

/**
 * Interceptor which for each TestNGMethod creates one or more RequirementAwareTestNGMethods.
 * More than one RequirementAwareTestNGMethods is created if test method requirements use
 * {@link com.teradata.tempto.Requirements#allOf(List)} clause.
 * <p>
 * Each of RequirementAwareTestNGMethods have one requirement set attached from {@link com.teradata.tempto.CompositeRequirement}
 * returned by {@link com.teradata.tempto.internal.RequirementsCollector} for given test method.
 */
public class RequirementsExpanderInterceptor
        implements IMethodInterceptor
{
    private static final String METHODS_COUNT_KEY = "RequirementsExpander.methods.count";

    private final TestSpecificRequirementsResolver testSpecificRequirementsResolver;

    private volatile int seenMethodsCount = 0;

    public RequirementsExpanderInterceptor()
    {
        this.testSpecificRequirementsResolver = new TestSpecificRequirementsResolver(createTestConfiguration());
    }

    public static int getMethodsCountFromContext(ITestContext context)
    {
        return (int) context.getAttribute(METHODS_COUNT_KEY);
    }

    @Override
    public synchronized List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context)
    {
        /*
         * For some unknown reason TestNG calls method interceptors more than once for
         * same methods set. We determine if we already seen method by looking at type of internal TestNGMethod field.
         */
        List<IMethodInstance> allExpandedMethods = Lists.newArrayList();
        for (IMethodInstance method : methods) {
            if (isMethodAlreadyExpanded(method)) {
                allExpandedMethods.add(method);
            }
            else {
                List<IMethodInstance> newExpandedMethods = expandMethod(method);
                incrementSeenMethodsCount(newExpandedMethods);
                allExpandedMethods.addAll(newExpandedMethods);
            }
        }

        context.setAttribute(METHODS_COUNT_KEY, seenMethodsCount);
        return allExpandedMethods;
    }

    private void incrementSeenMethodsCount(List<IMethodInstance> newExpandedMethods)
    {
        for (IMethodInstance newExpandedMethod : newExpandedMethods) {
            Optional<Object[][]> parametersForMethod = getParametersForMethod(newExpandedMethod);
            if (parametersForMethod.isPresent()) {
                seenMethodsCount += parametersForMethod.get().length;
            } else {
                seenMethodsCount++;
            }
        }
    }

    private Optional<Object[][]> getParametersForMethod(IMethodInstance method)
    {
        Test testAnnotation = method.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
        Class dataProviderClass = testAnnotation.dataProviderClass();
        if (dataProviderClass == null || dataProviderClass == Object.class) {
            dataProviderClass = method.getMethod().getRealClass();
        }
        String dataProviderName = testAnnotation.dataProvider();
        if (dataProviderName.isEmpty()) {
            return Optional.empty();
        }

        Optional<Method> dataProviderMethod = asList(dataProviderClass.getMethods()).stream().filter(
                m -> {
                    DataProvider annotation = m.getAnnotation(DataProvider.class);
                    return annotation != null && annotation.name().equals(dataProviderName);
                }
        ).findFirst();
        if (dataProviderMethod.isPresent()) {
            try {
                return Optional.of((Object[][]) dataProviderMethod.get().invoke(method.getInstance()));
            }
            catch (Exception e) {
                throw new RuntimeException("Exception while calling data provider for " + method, e);
            }
        }
        else {
            return Optional.empty();
        }
    }

    private boolean isMethodAlreadyExpanded(IMethodInstance method) {return method.getMethod() instanceof RequirementsAwareTestNGMethod;}

    private List<IMethodInstance> expandMethod(IMethodInstance method)
    {
        List<IMethodInstance> extendedMethods = Lists.newArrayList();
        Set<Set<Requirement>> testSpecificRequirements = testSpecificRequirementsResolver.resolve(method.getMethod());
        if (testSpecificRequirements.size() == 1) {
            extendedMethods.add(new MethodInstance(new RequirementsAwareTestNGMethod(method.getMethod(), getOnlyElement(testSpecificRequirements))));
        }
        else {
            for (Set<Requirement> requirementSet : testSpecificRequirements) {
                TestNGMethod clonedMethod = (TestNGMethod) method.getMethod().clone();
                extendedMethods.add(new MethodInstance(new RequirementsAwareTestNGMethod(clonedMethod, requirementSet)));
            }
        }
        return extendedMethods;
    }
}
