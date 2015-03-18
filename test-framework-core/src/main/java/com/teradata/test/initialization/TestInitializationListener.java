/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.initialization;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.teradata.test.Requirement;
import com.teradata.test.context.GuiceTestContext;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.fulfillment.table.ImmutableTableFulfiller;
import com.teradata.test.initialization.modules.TestConfigurationModule;
import com.teradata.test.initialization.modules.TestInfoModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.reverse;
import static com.google.inject.util.Modules.combine;
import static com.teradata.test.RequirementsCollector.collectRequirementsFor;
import static com.teradata.test.context.ThreadLocalTestContextHolder.clearTestContext;
import static com.teradata.test.context.ThreadLocalTestContextHolder.setTestContext;

public class TestInitializationListener
        extends TestSuiteAwareTestInvocationListener
{
    private final static Logger LOGGER = LoggerFactory.getLogger(TestInitializationListener.class);

    private final static List<Module> SUITE_MODULES = ImmutableList.of(
            new TestConfigurationModule(),
            new TestInfoModule("SUITE")
    );

    private final static List<Class<? extends RequirementFulfiller>> SUITE_FULFILLERS = ImmutableList.<Class<? extends RequirementFulfiller>>of(
            ImmutableTableFulfiller.class
    );

    private final static List<Class<? extends RequirementFulfiller>> TEST_METHOD_FULFILLERS = ImmutableList.<Class<? extends RequirementFulfiller>>of();

    private final List<Module> suiteModules;
    private final List<Class<? extends RequirementFulfiller>> suiteFulfillers;
    private final List<Class<? extends RequirementFulfiller>> testMethodFulfillers;

    private Optional<GuiceTestContext> suiteTestContext = Optional.empty();
    private Optional<GuiceTestContext> testMethodTextContext = Optional.empty();

    public TestInitializationListener()
    {
        this(SUITE_MODULES, SUITE_FULFILLERS, TEST_METHOD_FULFILLERS);
    }

    public TestInitializationListener(
            List<Module> suiteModules,
            List<Class<? extends RequirementFulfiller>> suiteFulfillers,
            List<Class<? extends RequirementFulfiller>> testMethodFulfillers)
    {
        this.suiteModules = suiteModules;
        this.suiteFulfillers = suiteFulfillers;
        this.testMethodFulfillers = testMethodFulfillers;
    }

    @Override
    public void beforeSuite(ITestContext context)
    {
        try {
            Set<Requirement> allTestsRequirements = getAllTestsRequirements(context);
            GuiceTestContext testContext = new GuiceTestContext(combine(suiteModules));
            doFulfillment(testContext, suiteFulfillers, allTestsRequirements);
            setSuiteTestContext(testContext);
        }
        catch (RuntimeException e) {
            LOGGER.error("cannot initialize test suite", e);
        }
    }

    private void doFulfillment(GuiceTestContext testContext,
            List<Class<? extends RequirementFulfiller>> fulillerClasses,
            Set<Requirement> requirements)
    {
        List<Class<? extends RequirementFulfiller>> successfulFulfillerClasses = newArrayList();
        runWithTestContext(testContext, () -> {
            try {
                for (Class<? extends RequirementFulfiller> fulfillerClass : fulillerClasses) {
                    RequirementFulfiller fulfiller = testContext.getDependency(fulfillerClass);
                    Set<State> states = fulfiller.fulfill(requirements);
                    for (State state : states) {
                        testContext.pushState(state);
                    }
                    successfulFulfillerClasses.add(fulfillerClass);
                }
            }
            catch (RuntimeException e) {
                doCleanup(testContext, successfulFulfillerClasses);
                throw e;
            }
        });
    }

    private void doCleanup(GuiceTestContext testContext, List<Class<? extends RequirementFulfiller>> fulillerClasses)
    {
        for (Class<? extends RequirementFulfiller> fulillerClass : reverse(fulillerClasses)) {
            testContext.getDependency(fulillerClass).cleanup();
        }
    }

    @Override
    public void afterSuite(ITestContext context)
    {
        checkState(suiteTestContext.isPresent());
        runWithTestContext(suiteTestContext.get(), () -> {
            doCleanup(suiteTestContext.get(), suiteFulfillers);
        });
    }

    @Override
    public void beforeTest(IInvokedMethod method, ITestResult testResult, ITestContext context)
    {
        checkState(suiteTestContext.isPresent(), "test suite not initialized");

        GuiceTestContext testContext = suiteTestContext.get().override(
                new TestInfoModule(testResult.getMethod().getClass().getName() + "." + testResult.getMethod().getMethodName()));
        Set<Requirement> testSpecificRequirements = getTestSpecificRequirements(testResult.getMethod());
        doFulfillment(testContext, testMethodFulfillers, testSpecificRequirements);
        setTestMethodTestContext(testContext);
    }

    @Override
    public void afterTest(IInvokedMethod method, ITestResult testResult, ITestContext context)
    {
        checkState(testMethodTextContext.isPresent());
        runWithTestContext(testMethodTextContext.get(), () -> {
            doCleanup(testMethodTextContext.get(), testMethodFulfillers);
        });

        unsetTestMethodTestContext();
    }

    private Set<Requirement> getAllTestsRequirements(ITestContext context)
    {
        Set<Requirement> allTestsRequirements = Sets.newHashSet();
        for (ITestNGMethod iTestNGMethod : context.getAllTestMethods()) {
            Set<Set<Requirement>> requirementSets = collectRequirementsFor(getJavaMethodFromTestMethod(iTestNGMethod));
            for (Set<Requirement> requirementSet : requirementSets) {
                allTestsRequirements.addAll(requirementSet);
            }
        }
        return allTestsRequirements;
    }

    private Set<Requirement> getTestSpecificRequirements(ITestNGMethod testMethod)
    {
        Set<Set<Requirement>> requirementSets = collectRequirementsFor(getJavaMethodFromTestMethod(testMethod));
        checkArgument(requirementSets.size() == 1, "multiple sets of requirements per test are not supported yet");
        return getOnlyElement(requirementSets);
    }

    private Method getJavaMethodFromTestMethod(ITestNGMethod method)
    {
        return method.getConstructorOrMethod().getMethod();
    }

    private void setSuiteTestContext(GuiceTestContext suiteTestContext)
    {
        checkState(!this.suiteTestContext.isPresent(), "suite fulfillment result already set");
        this.suiteTestContext = Optional.of(suiteTestContext);
    }

    private void setTestMethodTestContext(GuiceTestContext testMethodTestContext)
    {
        checkState(!this.testMethodTextContext.isPresent(), "test fulfillment result already set");
        this.testMethodTextContext = Optional.of(testMethodTestContext);
    }

    private void unsetTestMethodTestContext()
    {
        this.testMethodTextContext = Optional.empty();
    }

    private void runWithTestContext(GuiceTestContext testContext, Runnable runnable)
    {
        setTestContext(testContext);
        try {
            runnable.run();
        }
        finally {
            clearTestContext();
        }
    }
}
