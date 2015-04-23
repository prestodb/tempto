/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.initialization;

import com.teradata.test.CompositeRequirement;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.MethodInstance;
import org.testng.internal.TestNGMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.internal.RequirementsCollector.getAnnotationBasedRequirementsFor;

/**
 * Interceptor which for each TestNGMethod creates one or more RequirementAwareTestNGMethods.
 * More than one RequirementAwareTestNGMethods is created if test method requirements use
 * {@link com.teradata.test.Requirements#allOf(List)} clause.
 *
 * Each of RequirementAwareTestNGMethods have one requirement set attached from {@link CompositeRequirement}
 * returned by {@link com.teradata.test.internal.RequirementsCollector} for given test method.
 */
public class RequirementsExtender
        implements IMethodInterceptor
{
    private static final String METHODS_COUNT_KEY = "RequirementsExtender.methods.count";

    private volatile boolean alreadyCalled = false;
    private volatile List<IMethodInstance> newMethods = newArrayList();

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context)
    {
        /**
         * There is bug in TestNG and interceptor is called twice.
         * We skip second call.
         */
        if (alreadyCalled) {
            return newMethods;
        }

        for (IMethodInstance method : methods) {
            Set<Set<Requirement>> testSpecificRequirements = resolveTestSpecificRequirements(method.getMethod());
            if (testSpecificRequirements.size() == 1) {
                newMethods.add(new MethodInstance(new RequirementsAwareTestNGMethod(method.getMethod(), getOnlyElement(testSpecificRequirements))));
            }
            else {
                for (Set<Requirement> requirementSet : testSpecificRequirements) {
                    TestNGMethod clonedMethod = (TestNGMethod) method.getMethod().clone();
                    newMethods.add(new MethodInstance(new RequirementsAwareTestNGMethod(clonedMethod, requirementSet)));
                }
            }
        }

        context.setAttribute(METHODS_COUNT_KEY, newMethods.size());

        alreadyCalled = true;
        return newMethods;
    }

    public static Set<Set<Requirement>> resolveTestSpecificRequirements(ITestNGMethod testMethod)
    {
        Method javaTestMethod = getJavaMethodFromTestMethod(testMethod);
        CompositeRequirement compositeRequirement = getAnnotationBasedRequirementsFor(javaTestMethod);
        Optional<Requirement> providedRequirement = getExplicitRequirementsFor(testMethod.getInstance());
        if (providedRequirement.isPresent()) {
            compositeRequirement = compose(providedRequirement.get(), compositeRequirement);
        }

        Set<Set<Requirement>> requirementSets = compositeRequirement.getRequirementsSets();
        return requirementSets;
    }

    public static int getMethodsCountFromContext(ITestContext context) {
        return (int) context.getAttribute(METHODS_COUNT_KEY);
    }

    static Optional<Requirement> getExplicitRequirementsFor(Object testClassInstance)
    {
        if (testClassInstance instanceof RequirementsProvider) {
            return Optional.of(((RequirementsProvider) testClassInstance).getRequirements());
        }
        else {
            return Optional.empty();
        }
    }

    static Method getJavaMethodFromTestMethod(ITestNGMethod method)
    {
        return method.getConstructorOrMethod().getMethod();
    }
}
