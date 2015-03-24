/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.initialization

import com.google.inject.Inject
import com.teradata.test.Requirement
import com.teradata.test.RequirementsProvider
import com.teradata.test.Requires
import com.teradata.test.context.State
import com.teradata.test.context.TestContext
import com.teradata.test.fulfillment.RequirementFulfiller
import org.testng.IInvokedMethod
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.internal.ConstructorOrMethod
import spock.lang.Specification

import java.lang.reflect.Method

import static com.teradata.test.context.ThreadLocalTestContextHolder.assertTestContextNotSet
import static com.teradata.test.context.ThreadLocalTestContextHolder.assertTestContextSet

class TestInitializationListenerTest
        extends Specification
{
  static final A = 'A'
  static final B = 'B'
  static final C = 'C'

  static final A_FULFILL = 'AFULFILL'
  static final B_FULFILL = 'BFULFILL'
  static final C_FULFILL = 'CFULFILL'

  static final A_CLEANUP = 'ACLEANUP'
  static final B_CLEANUP = 'BCLEANUP'
  static final C_CLEANUP = 'CCLEANUP'

  static final A_CALLBACK = 'ACALLBACK'
  static final B_CALLBACK = 'BCALLBACK'
  static final C_CALLBACK = 'CCALLBACK'

  static List<Event> EVENTS

  def setup()
  {
    EVENTS = []
  }

  def 'should fulfill requirements'()
  {
    setup:
    def listener = new TestInitializationListener([], [AFulfiller], [BFulfiller])
    def iTestContext = getITestContext(successMethod)
    def iTestResult = getITestResult(successMethod)

    when:
    listener.onStart(iTestContext)
    assertTestContextNotSet()
    listener.onTestStart(iTestResult)
    assertTestContextSet()
    listener.onTestSuccess(iTestResult)
    assertTestContextNotSet()
    listener.onFinish(iTestContext)

    then:
    EVENTS[0].name == A_FULFILL
    EVENTS[1].name == B_FULFILL
    EVENTS[2].name == B_CLEANUP
    EVENTS[3].name == B_CALLBACK
    EVENTS[4].name == A_CLEANUP
    EVENTS[5].name == A_CALLBACK

    EVENTS[1].object == EVENTS[2].object
    EVENTS[0].object == EVENTS[4].object
  }

  def 'should cleanup after failure'()
  {
    setup:
    def listener = new TestInitializationListener([], [AFulfiller], [BFulfiller, CFulfiller])
    def iTestContext = getITestContext(failMethod)
    def iTestResult = getITestResult(failMethod)

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
    EVENTS[0].name == A_FULFILL
    EVENTS[1].name == B_FULFILL
    EVENTS[2].name == C_FULFILL
    EVENTS[3].name == B_CLEANUP
    EVENTS[4].name == B_CALLBACK
    EVENTS[5].name == C_CALLBACK
    EVENTS[6].name == A_CLEANUP
    EVENTS[7].name == A_CALLBACK

    EVENTS[1].object == EVENTS[3].object
    EVENTS[0].object == EVENTS[6].object
  }

  def getITestContext(Method method)
  {
    ITestContext suiteContext = Mock(ITestContext)
    ITestNGMethod testMethod = Mock(ITestNGMethod)

    suiteContext.allTestMethods >> [testMethod]
    testMethod.getConstructorOrMethod() >> new ConstructorOrMethod(method)

    return suiteContext
  }

  def getITestResult(Method method)
  {
    ITestResult testResult = Mock(ITestResult)
    ITestNGMethod testMethod = Mock(ITestNGMethod)

    testResult.method >> testMethod
    testMethod.method >> method
    testMethod.getConstructorOrMethod() >> new ConstructorOrMethod(method)
    return testResult
  }

  def getSuccessMethod()
  {
    return TestClass.getMethod('testMethodSuccess')
  }

  def getFailMethod()
  {
    return TestClass.getMethod('testMethodFailed')
  }

  @Requires(ARequirement)
  static class TestClass
  {

    @Requires(BRequirement)
    public void testMethodSuccess()
    {
    }

    @Requires([BRequirement, CRequirement])
    public void testMethodFailed()
    {
    }
  }

  static class ARequirement
          implements RequirementsProvider
  {
    @Override
    public Requirement getRequirements()
    {
      return new DummyRequirement(A)
    }
  }

  static class AFulfiller
          extends DummyFulfiller
  {
    @Inject
    AFulfiller(TestContext testContext)
    {
      super(A, A_FULFILL, A_CLEANUP, A_CALLBACK, testContext)
    }
  }

  static class BRequirement
          implements RequirementsProvider
  {
    @Override
    public Requirement getRequirements()
    {
      return new DummyRequirement(B)
    }
  }

  static class BFulfiller
          extends DummyFulfiller
  {
    @Inject
    BFulfiller(TestContext testContext)
    {
      super(B, B_FULFILL, B_CLEANUP, B_CALLBACK, testContext)
    }
  }

  static class CRequirement
          implements RequirementsProvider
  {
    @Override
    public Requirement getRequirements()
    {
      return new DummyRequirement(C)
    }
  }

  static class CFulfiller
          extends DummyFulfiller
  {
    @Inject
    CFulfiller(TestContext testContext)
    {
      super(C, C_FULFILL, C_CLEANUP, C_CALLBACK, testContext)
    }

    Set<State> fulfill(Set<Requirement> requirements)
    {
      super.fulfill(requirements)
      throw new RuntimeException()
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
      testContext.registerCloseCallback(new Runnable() {
        @Override
        void run()
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

    void cleanup()
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
