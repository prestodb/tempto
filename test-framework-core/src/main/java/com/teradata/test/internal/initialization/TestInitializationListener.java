/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.initialization;

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.teradata.test.AfterTestWithContext;
import com.teradata.test.BeforeTestWithContext;
import com.teradata.test.CompositeRequirement;
import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.context.TestContext;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;
import com.teradata.test.initialization.TestMethodModuleProvider;
import com.teradata.test.internal.context.GuiceTestContext;
import com.teradata.test.internal.context.TestContextStack;
import com.teradata.test.internal.fulfillment.table.ImmutableTablesFulfiller;
import com.teradata.test.internal.fulfillment.table.MutableTablesFulfiller;
import org.slf4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.context.ThreadLocalTestContextHolder.popAllTestContexts;
import static com.teradata.test.context.ThreadLocalTestContextHolder.pushAllTestContexts;
import static com.teradata.test.context.ThreadLocalTestContextHolder.runWithTextContext;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContextIfSet;
import static com.teradata.test.internal.ReflectionHelper.getAnnotatedSubTypesOf;
import static com.teradata.test.internal.ReflectionHelper.instantiate;
import static com.teradata.test.internal.RequirementsCollector.getAnnotationBasedRequirementsFor;
import static com.teradata.test.internal.configuration.TestConfigurationFactory.createTestConfiguration;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class TestInitializationListener
        implements ITestListener
{
    private static final Logger LOGGER = getLogger(TestInitializationListener.class);

    private final static List<Class<? extends RequirementFulfiller>> BUILTIN_SUITE_LEVEL_FULFILLERS = ImmutableList.of(
            ImmutableTablesFulfiller.class
    );

    private static final List<Class<? extends RequirementFulfiller>> BUILTIN_TEST_METHOD_LEVEL_FULFILLERS = ImmutableList.<Class<? extends RequirementFulfiller>>of(
            MutableTablesFulfiller.class
    );

    private final List<? extends SuiteModuleProvider> suiteModuleProviders;
    private final List<? extends TestMethodModuleProvider> testMethodModuleProviders;
    private final List<Class<? extends RequirementFulfiller>> suiteLevelFulfillers;
    private final List<Class<? extends RequirementFulfiller>> testMethodLevelFulfillers;
    private final Configuration configuration;

    private Optional<TestContextStack<GuiceTestContext>> suiteTestContextStack = Optional.empty();

    public TestInitializationListener()
    {
        this(getSuiteModuleProviders(), getTestMethodModuleProviders(),
                getSuiteLevelFulfillers(), getTestMethodLevelFulfillers(),
                createTestConfiguration());
    }

    private static List<Class<? extends RequirementFulfiller>> getTestMethodLevelFulfillers()
    {
        return scanFulfillers(BUILTIN_TEST_METHOD_LEVEL_FULFILLERS, RequirementFulfiller.AutoFulfillerTestLevel.class);
    }

    private static List<Class<? extends RequirementFulfiller>> getSuiteLevelFulfillers()
    {
        return scanFulfillers(BUILTIN_SUITE_LEVEL_FULFILLERS, RequirementFulfiller.AutoFulfillerSuiteLevel.class);
    }

    private static List<Class<? extends RequirementFulfiller>> scanFulfillers(List<Class<? extends RequirementFulfiller>> builtinFulfillers, Class<? extends Annotation> filterAnnotation)
    {
        ImmutableList.Builder<Class<? extends RequirementFulfiller>> resultFulfillers = ImmutableList.builder();
        resultFulfillers.addAll(builtinFulfillers);
        resultFulfillers.addAll(getAnnotatedSubTypesOf(RequirementFulfiller.class, filterAnnotation));
        return resultFulfillers.build();
    }

    public static List<? extends SuiteModuleProvider> getSuiteModuleProviders()
    {
        return instantiate(getAnnotatedSubTypesOf(SuiteModuleProvider.class, AutoModuleProvider.class));
    }

    public static List<? extends TestMethodModuleProvider> getTestMethodModuleProviders()
    {
        return instantiate(getAnnotatedSubTypesOf(TestMethodModuleProvider.class, AutoModuleProvider.class));
    }

    public TestInitializationListener(
            List<? extends SuiteModuleProvider> suiteModuleProviders,
            List<? extends TestMethodModuleProvider> testMethodModuleProviders,
            List<Class<? extends RequirementFulfiller>> suiteLevelFulfillers,
            List<Class<? extends RequirementFulfiller>> testMethodLevelFulfillers,
            Configuration configuration)
    {
        this.suiteModuleProviders = suiteModuleProviders;
        this.testMethodModuleProviders = testMethodModuleProviders;
        this.suiteLevelFulfillers = suiteLevelFulfillers;
        this.testMethodLevelFulfillers = testMethodLevelFulfillers;
        this.configuration = configuration;
    }

    @Override
    public void onStart(ITestContext context)
    {
        Module suiteModule = combine(combine(getSuiteModules()), bind(suiteLevelFulfillers), bind(testMethodLevelFulfillers));
        GuiceTestContext initSuiteTestContext = new GuiceTestContext(suiteModule);
        TestContextStack<GuiceTestContext> suiteTextContextStack = new TestContextStack<>();
        suiteTextContextStack.push(initSuiteTestContext);

        try {
            Set<Requirement> allTestsRequirements = getAllTestsRequirements(context);
            doFulfillment(suiteTextContextStack, suiteLevelFulfillers, allTestsRequirements);
        }
        catch (RuntimeException e) {
            LOGGER.error("cannot initialize test suite", e);
            throw e;
        }

        setSuiteTestContextStack(suiteTextContextStack);
    }

    @Override
    public void onFinish(ITestContext context)
    {
        if (!suiteTestContextStack.isPresent()) {
            return;
        }

        doCleanup(suiteTestContextStack.get(), suiteLevelFulfillers);
    }

    @Override
    public void onTestStart(ITestResult testResult)
    {
        checkState(suiteTestContextStack.isPresent(), "test suite not initialized");
        GuiceTestContext initTestContext = suiteTestContextStack.get().peek().createChildContext(emptyList(), getTestModules(testResult));
        TestContextStack<GuiceTestContext> testContextStack = new TestContextStack<>();
        testContextStack.push(initTestContext);

        try {
            Set<Requirement> testSpecificRequirements = getTestSpecificRequirements(testResult.getMethod());
            doFulfillment(testContextStack, testMethodLevelFulfillers, testSpecificRequirements);
        }
        catch (RuntimeException e) {
            LOGGER.debug("error within test initialization", e);
            throw e;
        }

        pushAllTestContexts(testContextStack);
        testContextStack.peek().injectMembers(testResult.getInstance());

        runBeforeWithContextMethods(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult result)
    {
        onTestFinished(result);
    }

    @Override
    public void onTestFailure(ITestResult result)
    {
        LOGGER.debug("test failure", result.getThrowable());
        onTestFinished(result);
    }

    @Override
    public void onTestSkipped(ITestResult result)
    {
        onTestFinished(result);
    }

    private void onTestFinished(ITestResult testResult)
    {
        if (!testContextIfSet().isPresent()) {
            return;
        }

        try {
            runAfterWithContextMethods(testResult);
        }
        finally {
            TestContextStack<GuiceTestContext> testContextStack = (TestContextStack) popAllTestContexts();
            doCleanup(testContextStack, testMethodLevelFulfillers);
        }
    }

    private void runBeforeWithContextMethods(ITestResult testResult)
    {
        try {
            invokeMethodsAnnotatedWith(BeforeTestWithContext.class, testResult);
        }
        catch (RuntimeException e) {
            TestContextStack<GuiceTestContext> testContextStack = (TestContextStack) popAllTestContexts();
            doCleanup(testContextStack, testMethodLevelFulfillers);
            throw e;
        }
    }

    private void runAfterWithContextMethods(ITestResult testResult)
    {
        invokeMethodsAnnotatedWith(AfterTestWithContext.class, testResult);
    }

    private static void invokeMethodsAnnotatedWith(Class<? extends Annotation> annotationClass, ITestResult testCase)
    {
        for (Method declaredMethod : testCase.getTestClass().getRealClass().getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(annotationClass) != null) {
                try {
                    declaredMethod.invoke(testCase.getInstance());
                }
                catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("error invoking methods annotated with " + annotationClass.getName(), e);
                }
            }
        }
    }

    private void doFulfillment(TestContextStack<GuiceTestContext> testContextStack,
            List<Class<? extends RequirementFulfiller>> fulfillerClasses,
            Set<Requirement> requirements)
    {
        List<Class<? extends RequirementFulfiller>> successfulFulfillerClasses = newArrayList();

        try {
            for (Class<? extends RequirementFulfiller> fulfillerClass : fulfillerClasses) {
                LOGGER.debug("Fulfilling using {}", fulfillerClass);
                GuiceTestContext testContext = testContextStack.peek();
                runWithTextContext(testContext, () -> {
                    RequirementFulfiller fulfiller = testContext.getDependency(fulfillerClass);
                    GuiceTestContext testContextWithNewStates = testContext.createChildContext(fulfiller.fulfill(requirements));
                    successfulFulfillerClasses.add(fulfillerClass);
                    testContextStack.push(testContextWithNewStates);
                });
            }
        }
        catch (RuntimeException e) {
            LOGGER.debug("error during fulfillment", e);
            doCleanup(testContextStack, successfulFulfillerClasses);
            throw e;
        }
    }

    private void doCleanup(TestContextStack<GuiceTestContext> testContextStack, List<Class<? extends RequirementFulfiller>> fulfillerClasses)
    {
        // one base test context plus one test context for each fulfiller
        checkState(testContextStack.size() == fulfillerClasses.size() + 1);

        for (Class<? extends RequirementFulfiller> fulfillerClass : reverse(fulfillerClasses)) {
            LOGGER.debug("Cleaning for fulfiller {}", fulfillerClass);
            TestContext testContext = testContextStack.pop();
            testContext.close();
            runWithTextContext(testContext, () -> testContextStack.peek().getDependency(fulfillerClass).cleanup());
        }

        // remove close init test context too
        testContextStack.peek().close();
    }

    private List<Module> getSuiteModules()
    {
        return suiteModuleProviders
                .stream()
                .map(provider -> provider.getModule(configuration))
                .collect(toList());
    }

    private List<Module> getTestModules(ITestResult testResult)
    {
        return testMethodModuleProviders
                .stream()
                .map(provider -> provider.getModule(configuration, testResult))
                .collect(toList());
    }

    private <T> Module bind(List<Class<? extends T>> classes)
    {
        List<Module> modules = Lists.transform(classes, new Function<Class<? extends T>, Module>()
        {
            public Module apply(Class<? extends T> clazz)
            {
                return (Binder binder) -> binder.bind(clazz).in(Singleton.class);
            }
        });
        return combine(modules);
    }

    private Set<Requirement> getAllTestsRequirements(ITestContext context)
    {
        Set<Requirement> allTestsRequirements = Sets.newHashSet();
        for (ITestNGMethod iTestNGMethod : context.getAllTestMethods()) {
            allTestsRequirements.addAll(getTestSpecificRequirements(iTestNGMethod));
        }
        return allTestsRequirements;
    }

    private Set<Requirement> getTestSpecificRequirements(ITestNGMethod testMethod)
    {
        Method javaTestMethod = getJavaMethodFromTestMethod(testMethod);
        CompositeRequirement compositeRequirement = getAnnotationBasedRequirementsFor(javaTestMethod);
        Optional<Requirement> providedRequirement = getExplicitRequirementsFor(testMethod.getInstance());
        if (providedRequirement.isPresent()) {
            compositeRequirement = compose(providedRequirement.get(), compositeRequirement);
        }

        Set<Set<Requirement>> requirementSets = compositeRequirement.getRequirementsSets();
        checkArgument(requirementSets.size() == 1, "multiple sets of requirements per test are not supported yet");
        return getOnlyElement(requirementSets);
    }

    private Optional<Requirement> getExplicitRequirementsFor(Object testClassInstance)
    {
        if (testClassInstance instanceof RequirementsProvider) {
            return Optional.of(((RequirementsProvider) testClassInstance).getRequirements());
        }
        else {
            return Optional.empty();
        }
    }

    private Method getJavaMethodFromTestMethod(ITestNGMethod method)
    {
        return method.getConstructorOrMethod().getMethod();
    }

    private void setSuiteTestContextStack(TestContextStack<GuiceTestContext> suiteTestContextStack)
    {
        checkState(!this.suiteTestContextStack.isPresent(), "suite fulfillment result already set");
        this.suiteTestContextStack = Optional.of(suiteTestContextStack);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result)
    {
    }
}
