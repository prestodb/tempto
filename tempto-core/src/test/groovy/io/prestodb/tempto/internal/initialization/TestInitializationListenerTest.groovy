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
package io.prestodb.tempto.internal.initialization

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import io.prestodb.tempto.AfterTestWithContext
import io.prestodb.tempto.BeforeTestWithContext
import io.prestodb.tempto.Requirement
import io.prestodb.tempto.RequirementsProvider
import io.prestodb.tempto.Requires
import io.prestodb.tempto.configuration.Configuration
import io.prestodb.tempto.context.State
import io.prestodb.tempto.context.TestContext
import io.prestodb.tempto.context.TestContextCloseCallback
import io.prestodb.tempto.fulfillment.RequirementFulfiller
import io.prestodb.tempto.fulfillment.TestStatus
import io.prestodb.tempto.internal.TestSpecificRequirementsResolver
import org.testng.IResultMap
import org.testng.ITestClass
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.internal.ConstructorOrMethod
import spock.lang.Specification

import java.lang.reflect.Method

import static com.google.common.collect.Iterables.getOnlyElement
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.assertTestContextNotSet
import static io.prestodb.tempto.context.ThreadLocalTestContextHolder.assertTestContextSet
import static io.prestodb.tempto.internal.configuration.EmptyConfiguration.emptyConfiguration
import static io.prestodb.tempto.internal.initialization.TestInitializationListener.collectFulfillers

class TestInitializationListenerTest
        extends Specification
{
    static final A = 'A'
    static final B = 'B'
    static final C = 'C'
    static final BUILTIN = 'BuiltIn'

    static final SUITE_A_FULFILL = 'AFULFILL'
    static final TEST_B_FULFILL = 'BFULFILL'
    static final THROWING_TEST_C_FULFILL = 'CFULFILL'
    static final BUILTIN_FULFLILL = 'BUILTIN_FULFILL'

    static final SUITE_A_CLEANUP = 'ACLEANUP'
    static final TEST_B_CLEANUP = 'BCLEANUP'
    static final THROWING_TEST_C_CLEANUP = 'CCLEANUP'
    static final BUILTIN_CLEANUP = 'BUILTIN_CLEANUP'

    static final SUITE_A_CALLBACK = 'ACALLBACK'
    static final TEST_B_CALLBACK = 'BCALLBACK'
    static final THROWING_TEST_C_CALLBACK = 'CCALLBACK'
    static final BUILTIN_CALLBACK = 'BUILTIN_CALLBACK'

    static final A_REQUIREMENT = new DummyRequirement(A)
    static final B_REQUIREMENT = new DummyRequirement(B)
    static final C_REQUIREMENT = new DummyRequirement(C)

    static List<Event> EVENTS

    def setup()
    {
        EVENTS = []
    }

    def testSpecificRequirementsResolver = new TestSpecificRequirementsResolver(emptyConfiguration())

    def 'positive flow'()
    {
        setup:
        def testClass = new TestClass()
        def listener = new TestInitializationListener([], [], [AFulfiller], [BFulfiller], emptyConfiguration())
        def iTestContext = getITestContext(successMethod, testClass)
        def iTestResult = getITestResult(successMethod, testClass)

        when:
        listener.onStart(iTestContext)
        assertTestContextNotSet()
        listener.onTestStart(iTestResult)
        assertTestContextSet()
        assert testClass.testContext != null
        listener.onTestSuccess(iTestResult)
        assertTestContextNotSet()
        listener.onFinish(iTestContext)

        then:
        EVENTS[0].name == SUITE_A_FULFILL
        EVENTS[1].name == TEST_B_FULFILL
        EVENTS[2].name == "beforeMethod";
        EVENTS[3].name == "afterMethod";
        EVENTS[4].name == TEST_B_CLEANUP
        EVENTS[5].name == TEST_B_CALLBACK
        EVENTS[6].name == SUITE_A_CLEANUP
        EVENTS[7].name == SUITE_A_CALLBACK

        EVENTS[1].object == EVENTS[4].object
        EVENTS[0].object == EVENTS[6].object
    }

    def 'failure during fulfillment'()
    {
        setup:
        def testClass = new TestClass()
        def listener = new TestInitializationListener([], [], [AFulfiller], [BFulfiller, CFulfiller], emptyConfiguration())
        def iTestContext = getITestContext(failMethod, testClass)
        def iTestResult = getITestResult(failMethod, testClass)

        when:
        listener.onStart(iTestContext)
        try {
            listener.onTestStart(iTestResult)
            assert false
        }
        catch (RuntimeException _) {
        }
        listener.onTestFailure(iTestResult)
        listener.onFinish(iTestContext)

        then:
        EVENTS[0].name == SUITE_A_FULFILL
        EVENTS[1].name == TEST_B_FULFILL
        EVENTS[2].name == THROWING_TEST_C_FULFILL
        EVENTS[3].name == THROWING_TEST_C_CALLBACK
        EVENTS[4].name == TEST_B_CLEANUP
        EVENTS[5].name == TEST_B_CALLBACK
        EVENTS[6].name == SUITE_A_CLEANUP
        EVENTS[7].name == SUITE_A_CALLBACK

        EVENTS[1].object == EVENTS[4].object
        EVENTS[0].object == EVENTS[6].object
    }

    def getITestContext(Method method, TestClass testClass)
    {
        ITestContext suiteContext = Mock(ITestContext)

        suiteContext.allTestMethods >> [getITestNGMethod(method, testClass, getITestClass())]

        IResultMap iResultMap = Mock(IResultMap)
        iResultMap.size() >> 0
        suiteContext.getFailedTests() >> iResultMap

        return suiteContext
    }

    def getITestResult(Method method, TestClass testClass)
    {
        ITestResult testResult = Mock(ITestResult)
        ITestClass iTestClass = getITestClass()
        testResult.method >> getITestNGMethod(method, testClass, iTestClass)
        testResult.testClass >> iTestClass
        testResult.instance >> testResult.method.instance
        iTestClass.realClass >> TestClass
        return testResult
    }

    private ITestClass getITestClass()
    {
        ITestClass iTestClass = Mock()
        iTestClass.name >> "MockTestClass"
        return iTestClass
    }

    def getITestNGMethod(Method method, TestClass testClass, ITestClass iTestClass)
    {
        ITestNGMethod testMethod = Mock(ITestNGMethod)
        testMethod.testClass >> iTestClass
        testMethod.method >> method
        testMethod.instance >> testClass
        testMethod.groups >> []
        testMethod.methodName >> "mockTestMethod"
        testMethod.getConstructorOrMethod() >> new ConstructorOrMethod(method)
        def requirements = testSpecificRequirementsResolver.resolve(testMethod);
        return new RequirementsAwareTestNGMethod(testMethod, getOnlyElement(requirements))
    }

    def getSuccessMethod()
    {
        return TestClass.getMethod('testMethodSuccess')
    }

    def getFailMethod()
    {
        return TestClass.getMethod('testMethodFailed')
    }

    def 'scan for user fulfillers should sort them by their priority'()
    {
        when:
        def fulfillers = collectFulfillers(ImmutableList.of(), RequirementFulfiller.AutoTestLevelFulfiller.class);

        then:
        fulfillers.size() == 3
        fulfillers[0] == BFulfiller.class // with priority -1
        fulfillers[1] == AFulfiller.class // with default priority 0
        fulfillers[2] == CFulfiller.class // with priority 5
    }

    def 'user fulfillers can be before system level fulfillers'()
    {
        when:
        def fulfillers = collectFulfillers(ImmutableList.of(BuiltInFulfiller.class), RequirementFulfiller.AutoTestLevelFulfiller.class)

        then:
        fulfillers.size() == 4
        fulfillers[0] == BFulfiller.class // with priority -1
        fulfillers[1] == BuiltInFulfiller.class // built in fulfiller
        fulfillers[2] == AFulfiller.class // with default priority 0
        fulfillers[3] == CFulfiller.class // with priority 5
    }

    @Requires(ARequirement)
    static class TestClass
            implements RequirementsProvider
    {
        @Inject
        TestContext testContext

        @BeforeTestWithContext
        void beforeMethod()
        {
            EVENTS.add(new Event("beforeMethod", this));
        }

        @AfterTestWithContext
        void afterMethod()
        {
            EVENTS.add(new Event("afterMethod", this));
        }

        void testMethodSuccess()
        {
        }

        @Requires(CRequirement)
        void testMethodFailed()
        {
        }

        @Override
        Requirement getRequirements(Configuration configuration)
        {
            return B_REQUIREMENT
        }
    }

    static class ARequirement
            implements RequirementsProvider
    {
        @Override
        Requirement getRequirements(Configuration configuration)
        {
            return A_REQUIREMENT
        }
    }

    @RequirementFulfiller.AutoTestLevelFulfiller
    static class AFulfiller
            extends DummyFulfiller
    {
        @Inject
        AFulfiller(TestContext testContext)
        {
            super(A, SUITE_A_FULFILL, SUITE_A_CLEANUP, SUITE_A_CALLBACK, testContext)
        }
    }

    @RequirementFulfiller.AutoTestLevelFulfiller(priority = -1)
    static class BFulfiller
            extends DummyFulfiller
    {
        @Inject
        BFulfiller(TestContext testContext)
        {
            super(B, TEST_B_FULFILL, TEST_B_CLEANUP, TEST_B_CALLBACK, testContext)
        }
    }

    static class CRequirement
            implements RequirementsProvider
    {
        @Override
        Requirement getRequirements(Configuration configuration)
        {
            return C_REQUIREMENT
        }
    }

    @RequirementFulfiller.AutoTestLevelFulfiller(priority = 5)
    static class CFulfiller
            extends DummyFulfiller
    {
        @Inject
        CFulfiller(TestContext testContext)
        {
            super(C, THROWING_TEST_C_FULFILL, THROWING_TEST_C_CLEANUP, THROWING_TEST_C_CALLBACK, testContext)
        }

        Set<State> fulfill(Set<Requirement> requirements)
        {
            super.fulfill(requirements)
            throw new RuntimeException()
        }
    }

    static class BuiltInFulfiller
            extends DummyFulfiller
    {
        @Inject
        BuiltInFulfiller(TestContext testContext)
        {
            super(BUILTIN, BUILTIN_FULFLILL, BUILTIN_CLEANUP, BUILTIN_CALLBACK, testContext)
        }
    }

    static class DummyFulfiller
            implements RequirementFulfiller
    {
        final String requirementName
        final String fulfillEventName
        final String cleanupEventName

        DummyFulfiller(
                String requirementName,
                String fulfillEventName,
                String cleanupEventName,
                String callbackEventName,
                TestContext testContext)
        {
            this.requirementName = requirementName
            this.fulfillEventName = fulfillEventName
            this.cleanupEventName = cleanupEventName

            registerCallback(testContext, callbackEventName)
        }

        void registerCallback(TestContext testContext, String callbackEventName)
        {
            testContext.registerCloseCallback(new TestContextCloseCallback() {
                @Override
                void testContextClosed(TestContext _)
                {
                    EVENTS.add(new Event(callbackEventName, this))
                }
            })
        }

        Set<State> fulfill(Set<Requirement> requirements)
        {
            if (!requirements.contains(new DummyRequirement(requirementName))) {
                return [];
            }

            EVENTS.add(new Event(fulfillEventName, this))
            return []
        }

        void cleanup(TestStatus testStatus)
        {
            EVENTS.add(new Event(cleanupEventName, this))
        }
    }

    static class DummyRequirement
            implements Requirement
    {
        final String name

        DummyRequirement(String name)
        {
            this.name = name
        }

        boolean equals(o)
        {
            if (this.is(o)) {
                return true
            }
            if (getClass() != o.class) {
                return false
            }

            DummyRequirement that = (DummyRequirement) o

            if (name != that.name) {
                return false
            }

            return true
        }

        int hashCode()
        {
            return name.hashCode()
        }
    }

    static class Event
    {
        final String name
        final Object object

        Event(String name, Object object)
        {
            this.name = name
            this.object = object
        }
    }
}
