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
import com.teradata.test.internal.configuration.YamlConfiguration;
import com.teradata.test.internal.context.GuiceTestContext;
import com.teradata.test.internal.context.TestContextStack;
import com.teradata.test.internal.fulfillment.hive.HiveTablesFulfiller;
import com.teradata.test.internal.initialization.modules.HadoopModule;
import com.teradata.test.internal.initialization.modules.TestConfigurationModule;
import com.teradata.test.internal.initialization.modules.TestInfoModule;
import com.teradata.test.internal.query.QueryExecutorModule;
import org.slf4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
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
import static com.teradata.test.internal.RequirementsCollector.getAnnotationBasedRequirementsFor;
import static org.slf4j.LoggerFactory.getLogger;

public class TestInitializationListener
        implements ITestListener
{
    private static final Logger LOGGER = getLogger(TestInitializationListener.class);

    private static final String TEST_CONFIGURATION_URI_KEY = "test-configuration";
    private static final String DEFAULT_TEST_CONFIGURATION_URI = "classpath:/test-configuration.yaml";
    private static final String CLASSPATH_PROTOCOL = "classpath:";

    private final static List<Class<? extends RequirementFulfiller>> SUITE_FULFILLERS = ImmutableList.of(
            HiveTablesFulfiller.class
    );

    private static final List<Class<? extends RequirementFulfiller>> TEST_METHOD_FULFILLERS = ImmutableList.<Class<? extends RequirementFulfiller>>of();

    private final List<Module> suiteModules;
    private final List<Class<? extends RequirementFulfiller>> suiteFulfillers;
    private final List<Class<? extends RequirementFulfiller>> testMethodFulfillers;

    private Optional<TestContextStack<GuiceTestContext>> suiteTestContextStack = Optional.empty();

    public TestInitializationListener()
    {
        this(createSuiteModules(), SUITE_FULFILLERS, TEST_METHOD_FULFILLERS);
    }

    private static List<Module> createSuiteModules()
    {
        Configuration testConfiguration = createTestConfiguration();
        return ImmutableList.of(
                new TestInfoModule("SUITE"),
                new TestConfigurationModule(testConfiguration),
                new QueryExecutorModule(testConfiguration),
                new HadoopModule()
        );
    }

    private static InputStream getConfigurationInputStream()
    {
        String testConfigurationUri = System.getProperty(TEST_CONFIGURATION_URI_KEY, DEFAULT_TEST_CONFIGURATION_URI);
        if (testConfigurationUri.startsWith(CLASSPATH_PROTOCOL)) {
            InputStream input = TestInitializationListener.class.getResourceAsStream(testConfigurationUri.substring(CLASSPATH_PROTOCOL.length()));
            if (input == null) {
                throw new IllegalArgumentException("test configuration URI is incorrect: " + testConfigurationUri);
            }
            return input;
        }
        else {
            try {
                return new URL(testConfigurationUri).openConnection().getInputStream();
            }
            catch (IOException e) {
                throw new IllegalArgumentException("test configuration URI is incorrect: " + testConfigurationUri);
            }
        }
    }

    private static Configuration createTestConfiguration()
    {
        try (InputStream configurationInputStream = getConfigurationInputStream()) {
            return new YamlConfiguration(configurationInputStream);
        }
        catch (IOException e) {
            throw new RuntimeException("could not parse configuration file", e);
        }
    }

    public TestInitializationListener(
            List<Module> suiteModules,
            List<Class<? extends RequirementFulfiller>> suiteFulfillers,
            List<Class<? extends RequirementFulfiller>> testMethodFulfillers)
    {
        this.suiteModules = ImmutableList.of(combine(suiteModules), bind(suiteFulfillers), bind(testMethodFulfillers));
        this.suiteFulfillers = suiteFulfillers;
        this.testMethodFulfillers = testMethodFulfillers;
    }

    @Override
    public void onStart(ITestContext context)
    {
        GuiceTestContext initSuiteTestContext = new GuiceTestContext(combine(suiteModules));
        TestContextStack<GuiceTestContext> suiteTextContextStack = new TestContextStack<>();
        suiteTextContextStack.push(initSuiteTestContext);

        try {
            Set<Requirement> allTestsRequirements = getAllTestsRequirements(context);
            doFulfillment(suiteTextContextStack, suiteFulfillers, allTestsRequirements);
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

        doCleanup(suiteTestContextStack.get(), suiteFulfillers);
    }

    @Override
    public void onTestStart(ITestResult testResult)
    {
        checkState(suiteTestContextStack.isPresent(), "test suite not initialized");
        GuiceTestContext initTestContext = suiteTestContextStack.get().peek().createChildContext(getTestModules(testResult));
        TestContextStack<GuiceTestContext> testContextStack = new TestContextStack<>();
        testContextStack.push(initTestContext);

        try {
            Set<Requirement> testSpecificRequirements = getTestSpecificRequirements(testResult.getMethod());
            doFulfillment(testContextStack, testMethodFulfillers, testSpecificRequirements);
        }
        catch (RuntimeException e) {
            LOGGER.debug("error within test initialization", e);
            throw e;
        }

        pushAllTestContexts(testContextStack);
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
            doCleanup(testContextStack, testMethodFulfillers);
        }
    }

    private void runBeforeWithContextMethods(ITestResult testResult)
    {
        try {
            invokeMethodsAnnotatedWith(BeforeTestWithContext.class, testResult);
        }
        catch (RuntimeException e) {
            TestContextStack<GuiceTestContext> testContextStack = (TestContextStack) popAllTestContexts();
            doCleanup(testContextStack, testMethodFulfillers);
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

    private TestInfoModule getTestModules(ITestResult testResult)
    {
        return new TestInfoModule(testResult.getMethod().getClass().getName() + "." + testResult.getMethod().getMethodName());
    }

    private void doFulfillment(TestContextStack<GuiceTestContext> testContextStack,
            List<Class<? extends RequirementFulfiller>> fulfillerClasses,
            Set<Requirement> requirements)
    {
        List<Class<? extends RequirementFulfiller>> successfulFulfillerClasses = newArrayList();

        try {
            for (Class<? extends RequirementFulfiller> fulfillerClass : fulfillerClasses) {
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

        for (Class<? extends RequirementFulfiller> fulillerClass : reverse(fulfillerClasses)) {
            TestContext testContext = testContextStack.pop();
            testContext.close();
            runWithTextContext(testContext, () -> testContextStack.peek().getDependency(fulillerClass).cleanup());
        }

        // remove close init test context too
        testContextStack.peek().close();
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
