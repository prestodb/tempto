/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.initialization

import com.teradata.test.Requirement
import com.teradata.test.RequirementsProvider
import com.teradata.test.Requires
import com.teradata.test.context.State
import com.teradata.test.fulfillment.RequirementFulfiller
import org.testng.IInvokedMethod
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.internal.ConstructorOrMethod
import spock.lang.Specification

import java.lang.reflect.Method

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

  static List<Event> events

  def setup()
  {
    events = []
  }

  def 'should fulfill requirements'()
  {
    setup:
    def listener = new TestInitializationListener([], [AFulfiller], [BFulfiller])
    def suiteContext = getSuiteContext(successMethod)
    def testContext = getTestContext(successMethod)

    when:
    listener.beforeSuite(suiteContext)
    listener.beforeTest(Mock(IInvokedMethod), testContext, suiteContext)
    listener.afterTest(Mock(IInvokedMethod), testContext, suiteContext)
    listener.afterSuite(suiteContext)

    then:
    events[0].name == A_FULFILL
    events[1].name == B_FULFILL
    events[2].name == B_CLEANUP
    events[3].name == A_CLEANUP

    events[1].object == events[2].object
    events[0].object == events[3].object
  }

  def 'should cleanup after failure'()
  {
    setup:
    def listener = new TestInitializationListener([], [AFulfiller], [BFulfiller, CFulfiller])
    def suiteContext = getSuiteContext(failMethod)
    def testContext = getTestContext(failMethod)

    when:
    listener.beforeSuite(suiteContext)
    try {
      listener.beforeTest(Mock(IInvokedMethod), testContext, suiteContext)
      assert false
    }
    catch (RuntimeException _) {
    }
    listener.afterTest(Mock(IInvokedMethod), testContext, suiteContext)
    listener.afterSuite(suiteContext)

    then:
    events[0].name == A_FULFILL
    events[1].name == B_FULFILL
    events[2].name == C_FULFILL
    events[3].name == B_CLEANUP
    events[4].name == A_CLEANUP

    events[1].object == events[3].object
    events[0].object == events[4].object
  }

  def getSuiteContext(Method method)
  {
    ITestContext suiteContext = Mock(ITestContext)
    ITestNGMethod testMethod = Mock(ITestNGMethod)
    ConstructorOrMethod constructorOrMethod = new ConstructorOrMethod(method)

    suiteContext.allTestMethods >> [testMethod]
    testMethod.getConstructorOrMethod() >> constructorOrMethod
    constructorOrMethod.method >> method

    return suiteContext
  }

  def getTestContext(Method method)
  {
    ITestResult testResult = Mock(ITestResult)
    ITestNGMethod testMethod = Mock(ITestNGMethod)
    ConstructorOrMethod constructorOrMethod = new ConstructorOrMethod(method)
    testResult.method >> testMethod
    testMethod.method >> method
    testMethod.getConstructorOrMethod() >> constructorOrMethod
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

  static class AFulfiller
          extends DummyFulfiller
  {
    AFulfiller()
    {
      super(A, A_FULFILL, A_CLEANUP)
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

  static class BFulfiller
          extends DummyFulfiller
  {
    BFulfiller()
    {
      super(B, B_FULFILL, B_CLEANUP)
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

  static class CFulfiller
          extends DummyFulfiller
  {
    CFulfiller()
    {
      super(C, C_FULFILL, C_CLEANUP)
    }

    Set<State> fulfill(Set<Requirement> requirements)
    {
      super.fulfill(requirements)
      throw new RuntimeException()
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

  static class DummyFulfiller
          implements RequirementFulfiller
  {
    final String requirementName
    final String fulfillEventName
    final String cleanupEventName

    DummyFulfiller(String requirementName, String fulfillEventName, String cleanupEventName)
    {
      this.requirementName = requirementName
      this.fulfillEventName = fulfillEventName
      this.cleanupEventName = cleanupEventName
    }

    Set<State> fulfill(Set<Requirement> requirements)
    {
      if (!requirements.contains(new DummyRequirement(requirementName))) {
        return [];
      }

      events.add(new Event(fulfillEventName, this))
      return []
    }

    void cleanup()
    {
      events.add(new Event(cleanupEventName, this))
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
