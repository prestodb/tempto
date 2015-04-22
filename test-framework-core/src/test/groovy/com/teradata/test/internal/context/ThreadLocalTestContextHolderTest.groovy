/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.context

import com.teradata.test.context.TestContext
import spock.lang.Specification

import static com.teradata.test.context.ThreadLocalTestContextHolder.*

class ThreadLocalTestContextHolderTest
        extends Specification
{
  void cleanup()
  {
    if (testContextIfSet().isPresent()) {
      popAllTestContexts();
    }
  }

  def "assertNotSet does not throw if unset"()
  {
    setup:
    assertTestContextNotSet()
    // works fine
  }

  def "assertNotSet throws if set"()
  {
    setup:
    pushTestContext(Mock(TestContext))

    when:
    assertTestContextNotSet()

    then:
    thrown(IllegalStateException)
  }

  def "getting testContext throws if not set"()
  {
    when:
    testContext()

    then:
    thrown(IllegalStateException)
  }

  def "getting testContext returns what was set"()
  {
    setup:
    TestContext mockTestContext = Mock()
    pushTestContext(mockTestContext)

    expect:
    testContext() == mockTestContext
  }

  def "test context should propagate from parent to child, but not between siblings"()
  {
    setup:
    TestContext mockTestContext = Mock()

    pushTestContext(mockTestContext)
    popTestContext()

    runAndWait(new Runnable() {
      @Override
      void run()
      {
        assertTestContextNotSet()
        pushTestContext(mockTestContext)
      }
    })

    runAndWait(new Runnable() {
      @Override
      void run()
      {
        assertTestContextNotSet()
      }
    })
  }

  def runAndWait(Runnable runnable)
  {
    def thread = new Thread(runnable)
    thread.start()
    thread.join()
  }
}
