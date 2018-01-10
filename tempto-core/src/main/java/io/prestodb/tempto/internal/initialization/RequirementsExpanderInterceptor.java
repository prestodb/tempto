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

package io.prestodb.tempto.internal.initialization;

import com.google.common.collect.Lists;
import io.prestodb.tempto.CompositeRequirement;
import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.Requirements;
import io.prestodb.tempto.internal.DataProviders;
import io.prestodb.tempto.internal.RequirementsCollector;
import io.prestodb.tempto.internal.TestSpecificRequirementsResolver;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.internal.MethodInstance;
import org.testng.internal.TestNGMethod;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.getOnlyElement;
import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.testConfiguration;

/**
 * Interceptor which for each TestNGMethod creates one or more RequirementAwareTestNGMethods.
 * More than one RequirementAwareTestNGMethods is created if test method requirements use
 * {@link Requirements#allOf(List)} clause.
 * <p>
 * Each of RequirementAwareTestNGMethods have one requirement set attached from {@link CompositeRequirement}
 * returned by {@link RequirementsCollector} for given test method.
 */
public class RequirementsExpanderInterceptor
        implements IMethodInterceptor
{
    private static final String METHODS_COUNT_KEY = "RequirementsExpander.methods.count";

    private final TestSpecificRequirementsResolver testSpecificRequirementsResolver;

    private volatile int seenMethodsCount = 0;

    public RequirementsExpanderInterceptor()
    {
        this.testSpecificRequirementsResolver = new TestSpecificRequirementsResolver(testConfiguration());
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
            Optional<Object[][]> parametersForMethod = DataProviders.getParametersForMethod(newExpandedMethod.getMethod());
            if (parametersForMethod.isPresent()) {
                seenMethodsCount += parametersForMethod.get().length;
            }
            else {
                seenMethodsCount++;
            }
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
