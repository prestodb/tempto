package com.teradata.test.initialization;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;
import com.teradata.test.fulfillment.table.ImmutableTableFulfiller;
import com.teradata.test.initialization.modules.TestConfigurationModule;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.util.List;
import java.util.Optional;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static com.google.common.base.Preconditions.checkState;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.util.Modules.combine;
import static com.google.inject.util.Modules.override;

public class TestInitializationListener
        extends TestSuiteAwareTestInvocationListener
{

    private Optional<FulfillmentResult> suiteFulfillmentResult;
    private Optional<FulfillmentResult> testFulfillmentResult;

    @Override
    public void beforeSuite(ITestContext context)
    {
        List<Requirement> allTestsRequirements = getAllTestsRequirements(context);

        // todo maybe list of modules and/or fulfiller should depend of test framework configuration provided by user
        List<Module> suiteModules = ImmutableList.of(
                new TestConfigurationModule()
        );

        List<Class<? extends RequirementFulfiller>> suiteLevelFulfillers = ImmutableList.<Class<? extends RequirementFulfiller>>of(
                ImmutableTableFulfiller.class
        );
        setSuiteFullmentResult(doFulfillment(combine(suiteModules), suiteLevelFulfillers, allTestsRequirements));
    }

    private FulfillmentResult doFulfillment(Module baseModule,
            List<Class<? extends RequirementFulfiller>> fulillerClasses,
            List<Requirement> requirements)
    {
        Module currentModule = baseModule;
        List<RequirementFulfiller> fulfillers = newArrayList();
        try {
            for (Class<? extends RequirementFulfiller> fulillerClass : fulillerClasses) {
                Injector currentInjector = createInjector(currentModule);
                RequirementFulfiller fulfiller = currentInjector.getInstance(fulillerClass);
                List<State> states = fulfiller.fulfill(requirements);
                currentModule = override(currentModule).with(new AbstractModule()
                {
                    @Override
                    protected void configure()
                    {
                        for (State state : states) {
                            bind((Class<State>) state.getClass()).toInstance(state);
                        }
                    }
                });
                fulfillers.add(fulfiller);
            }
            return new FulfillmentResult(fulfillers, createInjector(currentModule), currentModule);
        }
        catch (RuntimeException e) {
            doCleanup(fulfillers);
            throw e;
        }
    }

    private void doCleanup(FulfillmentResult fulfillmentResult)
    {
        doCleanup(fulfillmentResult.getFulfillers());
    }

    private void doCleanup(List<RequirementFulfiller> fulfillers)
    {
        for (RequirementFulfiller fulfiller : fulfillers) {
            fulfiller.cleanup();
        }
    }

    @Override
    public void afterSuite(ITestContext context)
    {
        if (suiteFulfillmentResult.isPresent()) {
            doCleanup(suiteFulfillmentResult.get());
        }
    }

    @Override
    public void beforeTest(IInvokedMethod method, ITestResult testResult, ITestContext context)
    {
        List<Requirement> testSpecificRequirements = getTestSpecificRequirements(method);

        Module suiteGuiceModule = suiteFulfillmentResult.get().getGuiceModule();
        // todo do we actually need to extend list of guice modules here
        Module testGuiceModule = combine(
                suiteGuiceModule
        );
        List<Class<? extends RequirementFulfiller>> testLevelFulfillers = ImmutableList.<Class<? extends RequirementFulfiller>>of();
        setTestFulfillmentResult(doFulfillment(testGuiceModule, testLevelFulfillers, testSpecificRequirements));

        // todo create TestContext here and set it in test
        // todo store TestContext in some ThreadLocal for static helpers
    }

    @Override
    public void afterTest(IInvokedMethod method, ITestResult testResult, ITestContext context)
    {
        if (testFulfillmentResult.isPresent()) {
            doCleanup(testFulfillmentResult.get());
        }
        // todo remove TestContext from ThreadLocal

        unsetTestFulfillmentResult();
    }

    private List<Requirement> getAllTestsRequirements(ITestContext context)
    {
        // todo implement
        throw new RuntimeException("not implemented");
    }

    private List<Requirement> getTestSpecificRequirements(IInvokedMethod method)
    {
        // todo implement
        throw new RuntimeException("not implemented");
    }

    private void setSuiteFullmentResult(FulfillmentResult fulfillmentResult)
    {
        checkState(!this.suiteFulfillmentResult.isPresent(), "suite fulfillment result already set to %s", suiteFulfillmentResult);
        this.suiteFulfillmentResult = Optional.of(fulfillmentResult);
    }

    private void setTestFulfillmentResult(FulfillmentResult fulfillmentResult)
    {
        checkState(!this.testFulfillmentResult.isPresent(), "test fulfillment result already set to %s", testFulfillmentResult);
        this.testFulfillmentResult = Optional.of(fulfillmentResult);
    }

    private void unsetTestFulfillmentResult()
    {
        this.testFulfillmentResult = Optional.empty();
    }

    private static class FulfillmentResult
    {
        // Instances of all executed fulfillers. For sake of cleanup.
        private final List<RequirementFulfiller> fulfillers;
        // Final Guice Injector built based on base modules passed to doFulfillment method expanded with State instances obtained during fulfillers execution.
        private final Injector guiceInjector;
        // Guice module matching bindings of Injector
        private final Module guiceModule; // todo maybe this is not needed. one can probably use injector.getAllBindings and wrap that into module for DSLs operation

        public FulfillmentResult(List<RequirementFulfiller> fulfillers, Injector guiceInjector, Module guiceModule)
        {
            this.fulfillers = fulfillers;
            this.guiceInjector = guiceInjector;
            this.guiceModule = guiceModule;
        }

        public List<RequirementFulfiller> getFulfillers()
        {
            return fulfillers;
        }

        public Injector getGuiceInjector()
        {
            return guiceInjector;
        }

        public Module getGuiceModule()
        {
            return guiceModule;
        }
    }
}
