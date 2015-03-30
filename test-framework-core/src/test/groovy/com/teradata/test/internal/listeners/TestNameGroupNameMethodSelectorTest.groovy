/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.listeners

import org.testng.IMethodSelectorContext
import org.testng.ITestNGMethod
import spock.lang.Specification
import spock.lang.Unroll

class TestNameGroupNameMethodSelectorTest
        extends Specification
{

  private TestMetadataReader metadataReader = Mock()

  @Unroll
  def 'test selector match #testName/#testGroups for #allowedTestNames/#allowedTestGroups/#excludedTestGroups'()
  {
    setup:
    metadataReader.getTestMetadata(_) >> new TestMetadataReader.TestMetadata(testGroups as Set, testName)
    def testSelector = new TestNameGroupNameMethodSelector(asSetOptional(allowedTestNames),
            asSetOptional(allowedTestGroups),
            asSetOptional(excludedTestGroups),
            metadataReader);

    expect:
    testSelector.includeMethod(Mock(IMethodSelectorContext), Mock(ITestNGMethod), true) == expected

    where:
    testName    | testGroups   | allowedTestNames | allowedTestGroups | excludedTestGroups | expected
    'abc'       | ['g1', 'g2'] | null             | null              | null               | true
    'abc'       | ['g1', 'g2'] | ['abc']          | null              | null               | true
    'abc'       | ['g1', 'g2'] | ['xyz', 'abc']   | null              | null               | true
    'abc'       | ['g1', 'g2'] | null             | ['g1']            | null               | true
    'abc'       | ['g1', 'g2'] | null             | ['g1', 'g3']      | null               | true
    'abc'       | ['g1', 'g2'] | ['xyz', 'abc']   | ['g1', 'g3']      | null               | true
    'p.q.r.abc' | []           | ['abc']          | null              | null               | true
    'p.q.r.abc' | []           | ['r.abc']        | null              | null               | true
    'p.q.r.abc' | []           | ['p.q.r.abc']    | null              | null               | true
    'p.q.r.abc' | []           | ['bc']           | null              | null               | false
    'abc'       | ['g1', 'g2'] | ['xyz', 'abc']   | ['g1', 'g3']      | ['g1']             | false
    'abc'       | ['g1', 'g2'] | ['xyz', 'abc']   | ['g1', 'g3']      | ['g2']             | false
    'abc'       | ['g1', 'g2'] | ['xyz', 'abc']   | ['g1', 'g3']      | ['g5']             | true
  }

  private Optional<Set<String>> asSetOptional(List<String> strings)
  {
    if (strings == null) {
      return Optional.empty();
    }
    else {
      return Optional.of(strings as Set);
    }
  }
}
