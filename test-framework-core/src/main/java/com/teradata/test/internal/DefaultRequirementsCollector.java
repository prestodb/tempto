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

import com.google.common.collect.Lists;
import com.teradata.test.CompositeRequirement;
import com.teradata.test.Requirement;
import com.teradata.test.Requirements;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.Requires;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.teradata.test.Requirements.compose;
import static java.util.Arrays.asList;

/**
 * This class gathers requirements for a given test method.
 */
public class DefaultRequirementsCollector
        implements RequirementsCollector
{

    private static final RequirementsCollector INSTANCE = new DefaultRequirementsCollector();

    public static RequirementsCollector getInstance() {
        return INSTANCE;
    }

    private DefaultRequirementsCollector()
    {
    }

    @Override
    public CompositeRequirement getAnnotationBasedRequirementsFor(Method method)
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
                return provider.getRequirements();
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
