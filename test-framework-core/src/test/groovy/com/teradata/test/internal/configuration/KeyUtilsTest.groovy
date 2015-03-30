/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.configuration

import spock.lang.Specification

import static com.teradata.test.configuration.KeyUtils.getKeyPrefix
import static com.teradata.test.configuration.KeyUtils.joinKey
import static com.teradata.test.configuration.KeyUtils.splitKey

class KeyUtilsTest
        extends Specification
{
  def "test split key"()
  {
    expect:
    splitKey('abc') == ['abc']
    splitKey('a.b.c') == ['a', 'b', 'c']
  }

  def "join key"()
  {
    expect:
    joinKey(['a', 'b', 'c']) == 'a.b.c'
    joinKey(['a', null, 'c']) == 'a.c'
    joinKey([null , 'b', 'c']) == 'b.c'
    joinKey('a' , 'b', 'c') == 'a.b.c'
  }

  def "get key prefix"()
  {
    expect:
    getKeyPrefix('a.b.c', 1) == 'a'
    getKeyPrefix('a.b.c', 2) == 'a.b'
    getKeyPrefix('a.b.c', 3) == 'a.b.c'
    getKeyPrefix('a.b.c', 4) == 'a.b.c'
  }
}
