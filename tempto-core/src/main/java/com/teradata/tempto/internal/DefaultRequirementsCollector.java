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

package com.teradata.tempto.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.teradata.tempto.CompositeRequirement;
import com.teradata.tempto.Requirement;
import com.teradata.tempto.Requirements;
import com.teradata.tempto.RequirementsProvider;
import com.teradata.tempto.Requires;
import com.teradata.tempto.configuration.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.teradata.tempto.Requirements.compose;
import static java.util.Arrays.asList;

/**
 * This class gathers requirements for a given test method.
 */
public class DefaultRequirementsCollector
        implements RequirementsCollector
{
    private final List<RequirementsCollector> collectors;

    public DefaultRequirementsCollector(Configuration configuration)
    {
        this.collectors = ImmutableList.of(new RequirementsFromAnnotationCollector(configuration));
    }

    @Override
    public CompositeRequirement collect(Method method)
    {
        CompositeRequirement requirement = Requirements.compose();
        for (RequirementsCollector collector : collectors) {
            requirement = compose(requirement, collector.collect(method));
        }
        return requirement;
    }

    private static class RequirementsFromAnnotationCollector
            implements RequirementsCollector
    {
        private final Configuration configuration;

        public RequirementsFromAnnotationCollector(Configuration configuration)
        {
            this.configuration = configuration;
        }

        @Override
        public CompositeRequirement collect(Method method)
        {
            Requires methodRequiresAnnotation = method.getAnnotation(Requires.class);
            CompositeRequirement methodCompositeRequirement = getCompositeRequirement(methodRequiresAnnotation);

            Requires classRequiresAnnotation = method.getDeclaringClass().getAnnotation(Requires.class);
            CompositeRequirement classCompositeRequirement = getCompositeRequirement(classRequiresAnnotation);

            return compose(methodCompositeRequirement, classCompositeRequirement);
        }

        private CompositeRequirement getCompositeRequirement(Requires requires)
        {
            if (requires != null) {
                checkArgument(requires.value() != null);
                return Requirements.compose(toRequirements(requires.value()));
            }
            else {
                return compose();
            }
        }

        private List<Requirement> toRequirements(Class<? extends RequirementsProvider>[] providers)
        {
            return Lists.transform(asList(providers), (Class<? extends RequirementsProvider> providerClass) -> {
                try {
                    Constructor<? extends RequirementsProvider> constructor = providerClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    RequirementsProvider provider = constructor.newInstance();
                    return provider.getRequirements(configuration);
                }
                catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Could not instantiate provider class", e);
                }
                catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("No parameterless constructor for " + providerClass, e);
                }
            });
        }
    }
}
