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
package com.teradata.test.internal;

import com.teradata.test.CompositeRequirement;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.internal.DefaultRequirementsCollector;
import com.teradata.test.internal.RequirementsCollector;
import org.testng.ITestNGMethod;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import static com.teradata.test.Requirements.compose;

public class TestSpecificRequirementsResolver
{
    private final RequirementsCollector requirementsCollector;
    private final Configuration configuration;

    public TestSpecificRequirementsResolver(Configuration configuration)
    {
        this.requirementsCollector = new DefaultRequirementsCollector(configuration);
        this.configuration = configuration;
    }

    public Set<Set<Requirement>> resolve(ITestNGMethod testMethod)
    {
        Method javaTestMethod = getJavaMethodFromTestMethod(testMethod);
        CompositeRequirement compositeRequirement = requirementsCollector.getAnnotationBasedRequirementsFor(javaTestMethod);
        Optional<Requirement> providedRequirement = getExplicitRequirementsFor(testMethod.getInstance());
        if (providedRequirement.isPresent()) {
            compositeRequirement = compose(providedRequirement.get(), compositeRequirement);
        }

        return compositeRequirement.getRequirementsSets();
    }

    private Optional<Requirement> getExplicitRequirementsFor(Object testClassInstance)
    {
        if (testClassInstance instanceof RequirementsProvider) {
            return Optional.of(((RequirementsProvider) testClassInstance).getRequirements(configuration));
        }
        else {
            return Optional.empty();
        }
    }

    private Method getJavaMethodFromTestMethod(ITestNGMethod method)
    {
        return method.getConstructorOrMethod().getMethod();
    }
}
