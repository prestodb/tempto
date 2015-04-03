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
      clearTestContext();
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
    setTestContext(Mock(TestContext))

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
    setTestContext(mockTestContext)

    expect:
    testContext() == mockTestContext
  }
}
