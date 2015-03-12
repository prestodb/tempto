/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context

import spock.lang.Specification

import static com.teradata.test.context.ThreadLocalTestContextHolder.assertTestContextNotSet
import static com.teradata.test.context.ThreadLocalTestContextHolder.clearTestContext
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext

class ThreadLocalTestContextHolderTest
        extends Specification
{

  void cleanup()
  {
    clearTestContext();
  }

  def "assertNotSet does not throw if unset"() {
    setup:
    clearTestContext()
    assertTestContextNotSet()
    // works fine
  }

  def "assertNotSet throws if set"() {
    setup:
    setTestContext(Mock(TestContext))

    when:
    assertTestContextNotSet()

    then:
    thrown(IllegalStateException)
  }

  def "getting testContext throws if not set"() {
    setup:
    clearTestContext()

    when:
    testContext()

    then:
    thrown(IllegalStateException)
  }

  def "getting testContext returns what was set"() {
    setup:
    TestContext mockTestContext = Mock()
    setTestContext(mockTestContext)

    expect:
    testContext() == mockTestContext
  }

}
