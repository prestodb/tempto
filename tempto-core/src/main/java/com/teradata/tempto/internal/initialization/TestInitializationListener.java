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

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.teradata.tempto.AfterTestWithContext;
import com.teradata.tempto.BeforeTestWithContext;
import com.teradata.tempto.Requirement;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.context.TestContext;
import com.teradata.tempto.fulfillment.RequirementFulfiller;
import com.teradata.tempto.fulfillment.RequirementFulfiller.AutoSuiteLevelFulfiller;
import com.teradata.tempto.fulfillment.RequirementFulfiller.AutoTestLevelFulfiller;
import com.teradata.tempto.fulfillment.TestStatus;
import com.teradata.tempto.initialization.AutoModuleProvider;
import com.teradata.tempto.initialization.SuiteModuleProvider;
import com.teradata.tempto.initialization.TestMethodModuleProvider;
import com.teradata.tempto.internal.ReflectionInjectorHelper;
import com.teradata.tempto.internal.RequirementFulfillerByPriorityComparator;
import com.teradata.tempto.internal.TestSpecificRequirementsResolver;
import com.teradata.tempto.internal.context.GuiceTestContext;
import com.teradata.tempto.internal.context.TestContextStack;
import com.teradata.tempto.internal.fulfillment.table.ImmutableTablesFulfiller;
import com.teradata.tempto.internal.fulfillment.table.MutableTablesCleaner;
import com.teradata.tempto.internal.fulfillment.table.MutableTablesFulfiller;
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
import java.util.TimeZone;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.reverse;
import static com.google.inject.util.Modules.combine;
import static com.teradata.tempto.context.TestContextDsl.runWithTestContext;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.assertTestContextNotSet;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.popAllTestContexts;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.pushAllTestContexts;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.tempto.context.ThreadLocalTestContextHolder.testContextIfSet;
import static com.teradata.tempto.fulfillment.TestStatus.FAILURE;
import static com.teradata.tempto.fulfillment.TestStatus.SUCCESS;
import static com.teradata.tempto.internal.ReflectionHelper.getAnnotatedSubTypesOf;
import static com.teradata.tempto.internal.ReflectionHelper.instantiate;
import static com.teradata.tempto.internal.RequirementFulfillerPriorityHelper.getPriority;
import static com.teradata.tempto.internal.configuration.TestConfigurationFactory.createTestConfiguration;
import static com.teradata.tempto.internal.logging.LoggingMdcHelper.cleanLoggingMdc;
import static com.teradata.tempto.internal.logging.LoggingMdcHelper.setupLoggingMdcForTest;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class TestInitializationListener
        implements ITestListener
{
    private static final Logger LOGGER = getLogger(TestInitializationListener.class);

    private final static List<Class<? extends RequirementFulfiller>> BUILTIN_SUITE_LEVEL_FULFILLERS = ImmutableList.of(
            MutableTablesCleaner.class,
            ImmutableTablesFulfiller.class
    );

    private static final List<Class<? extends RequirementFulfiller>> BUILTIN_TEST_METHOD_LEVEL_FULFILLERS = ImmutableList.of(
            MutableTablesFulfiller.class
    );

    private final List<? extends SuiteModuleProvider> suiteModuleProviders;
    private final List<? extends TestMethodModuleProvider> testMethodModuleProviders;
    private final List<Class<? extends RequirementFulfiller>> suiteLevelFulfillers;
    private final List<Class<? extends RequirementFulfiller>> testMethodLevelFulfillers;
    private final ReflectionInjectorHelper reflectionInjectorHelper = new ReflectionInjectorHelper();

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
        return collectFulfillers(BUILTIN_TEST_METHOD_LEVEL_FULFILLERS, AutoTestLevelFulfiller.class);
    }

    private static List<Class<? extends RequirementFulfiller>> getSuiteLevelFulfillers()
    {
        return collectFulfillers(BUILTIN_SUITE_LEVEL_FULFILLERS, AutoSuiteLevelFulfiller.class);
    }

    static List<Class<? extends RequirementFulfiller>> collectFulfillers(List<Class<? extends RequirementFulfiller>> builtinFulfillers, Class<? extends Annotation> filterAnnotation)
    {
        List<Class<? extends RequirementFulfiller>> allFulfillers = scanForFulfillersAndSort(filterAnnotation);
        allFulfillers.addAll(getBuiltInFulfillerPosition(allFulfillers), builtinFulfillers);

        return ImmutableList.copyOf(allFulfillers);
    }

    private static int getBuiltInFulfillerPosition(List<Class<? extends RequirementFulfiller>> allFulfillers)
    {
        for (int i = 0; i < allFulfillers.size(); i++) {
            // Insert the built-in fulfillers before priority 0 user fulfillers
            if (getPriority(allFulfillers.get(i)) >= 0) {
                return i;
            }
        }

        return allFulfillers.size();
    }

    // package scope due testing
    static List<Class<? extends RequirementFulfiller>> scanForFulfillersAndSort(Class<? extends Annotation> filterAnnotation)
    {
        return getAnnotatedSubTypesOf(RequirementFulfiller.class, filterAnnotation)
                .stream()
                .sorted(new RequirementFulfillerByPriorityComparator())
                .collect(toList());
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
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Module suiteModule = combine(combine(getSuiteModules()), bind(suiteLevelFulfillers), bind(testMethodLevelFulfillers));
        GuiceTestContext initSuiteTestContext = new GuiceTestContext(suiteModule);
        TestContextStack<GuiceTestContext> suiteTextContextStack = new TestContextStack<>();
        suiteTextContextStack.push(initSuiteTestContext);

        try {
            Set<Requirement> allTestsRequirements = resolveAllTestsRequirements(context);
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

        TestStatus testStatus = context.getFailedTests().size() > 0 ? FAILURE : SUCCESS;
        doCleanup(suiteTestContextStack.get(), suiteLevelFulfillers, testStatus);
    }

    @Override
    public void onTestStart(ITestResult testResult)
    {
        setupLoggingMdcForTest(testResult);
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

        assertTestContextNotSet();
        pushAllTestContexts(testContextStack);
        GuiceTestContext topTestContext = testContextStack.peek();
        topTestContext.injectMembers(testResult.getInstance());

        runBeforeWithContextMethods(testResult, topTestContext);
    }

    @Override
    public void onTestSuccess(ITestResult result)
    {
        onTestFinished(result, SUCCESS);
    }

    @Override
    public void onTestFailure(ITestResult result)
    {
        LOGGER.debug("test failure", result.getThrowable());
        onTestFinished(result, FAILURE);
    }

    @Override
    public void onTestSkipped(ITestResult result)
    {
        onTestFinished(result, SUCCESS);
    }

    private void onTestFinished(ITestResult testResult, TestStatus testStatus)
    {
        if (!testContextIfSet().isPresent()) {
            return;
        }

        boolean runAfterSucceeded = false;
        try {
            runAfterWithContextMethods(testResult, (GuiceTestContext) testContext());
            runAfterSucceeded = true;
        }
        finally {
            TestContextStack<GuiceTestContext> testContextStack = (TestContextStack) popAllTestContexts();
            doCleanup(testContextStack, testMethodLevelFulfillers, runAfterSucceeded ? testStatus : FAILURE);
            cleanLoggingMdc();
        }
    }

    private void runBeforeWithContextMethods(ITestResult testResult, GuiceTestContext testContext)
    {
        try {
            invokeMethodsAnnotatedWith(BeforeTestWithContext.class, testResult, testContext);
        }
        catch (RuntimeException e) {
            TestContextStack<GuiceTestContext> testContextStack = (TestContextStack) popAllTestContexts();
            doCleanup(testContextStack, testMethodLevelFulfillers, FAILURE);
            throw e;
        }
    }

    private void runAfterWithContextMethods(ITestResult testResult, GuiceTestContext testContext)
    {
        invokeMethodsAnnotatedWith(AfterTestWithContext.class, testResult, testContext);
    }

    private void invokeMethodsAnnotatedWith(Class<? extends Annotation> annotationClass, ITestResult testCase, GuiceTestContext testContext)
    {
        for (Method declaredMethod : testCase.getTestClass().getRealClass().getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(annotationClass) != null) {
                try {
                    declaredMethod.invoke(testCase.getInstance(), reflectionInjectorHelper.getMethodArguments(testContext.getInjector(), declaredMethod));
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
                runWithTestContext(testContext, () -> {
                    RequirementFulfiller fulfiller = testContext.getDependency(fulfillerClass);
                    GuiceTestContext testContextWithNewStates = testContext.createChildContext(fulfiller.fulfill(requirements));
                    successfulFulfillerClasses.add(fulfillerClass);
                    testContextStack.push(testContextWithNewStates);
                });
            }
        }
        catch (RuntimeException e) {
            LOGGER.debug("error during fulfillment", e);
            doCleanup(testContextStack, successfulFulfillerClasses, FAILURE);
            throw e;
        }
    }

    private void doCleanup(TestContextStack<GuiceTestContext> testContextStack, List<Class<? extends RequirementFulfiller>> fulfillerClasses, TestStatus testStatus)
    {
        // one base test context plus one test context for each fulfiller
        checkState(testContextStack.size() == fulfillerClasses.size() + 1);

        for (Class<? extends RequirementFulfiller> fulfillerClass : reverse(fulfillerClasses)) {
            LOGGER.debug("Cleaning for fulfiller {}", fulfillerClass);
            TestContext testContext = testContextStack.pop();
            testContext.close();
            runWithTestContext(testContext, () -> testContextStack.peek().getDependency(fulfillerClass).cleanup(testStatus));
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

    private Set<Requirement> resolveAllTestsRequirements(ITestContext context)
    {
        // we cannot assume that context contains RequirementsAwareTestNGMethod instances here
        // as interceptor is for some reason called after onStart() which uses this method.
        Set<Requirement> allTestsRequirements = Sets.newHashSet();
        for (ITestNGMethod iTestNGMethod : context.getAllTestMethods()) {
            Set<Set<Requirement>> requirementsSets = new TestSpecificRequirementsResolver(configuration).resolve(iTestNGMethod);
            for (Set<Requirement> requirementsSet : requirementsSets) {
                allTestsRequirements.addAll(requirementsSet);
            }
        }
        return allTestsRequirements;
    }

    private Set<Requirement> getTestSpecificRequirements(ITestNGMethod testMethod)
    {
        return ((RequirementsAwareTestNGMethod) testMethod).getRequirements();
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
