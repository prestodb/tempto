/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.context

import com.google.inject.Binder
import com.google.inject.Module
import spock.lang.Specification

import static com.google.inject.util.Modules.EMPTY_MODULE

class GuiceTestContextTest
        extends Specification
{
  private static final def A = 'A'
  private static final def B = 'B'

  def 'test get dependency'()
  {
    setup:
    def context = new GuiceTestContext(EMPTY_MODULE)
    def state = new DummyState()

    expect:
    context.pushState(state)
    assert context.getDependency(DummyState) == state
  }

  def 'test override'()
  {
    setup:
    def state1 = new DummyState()
    def state2 = new DummyState()
    def context1 = new GuiceTestContext(new Module() {
      @Override
      void configure(Binder binder)
      {
        binder.bind(DummyState).toInstance(state1)
      }
    })
    def context2 = context1.override(new Module() {
      @Override
      void configure(Binder binder)
      {
        binder.bind(DummyState).toInstance(state2)
      }
    })

    expect:
    assert context1.getDependency(DummyState) == state1
    assert context2.getDependency(DummyState) == state2
  }

  def 'test states push/pop'()
  {
    setup:
    def context = new GuiceTestContext(EMPTY_MODULE)
    def state1 = new DummyState()
    def state2 = new DummyState()
    def state3 = new DummyState()

    expect:
    context.pushState(state1, A)
    assert context.getDependency(DummyState, A) == state1

    context.pushState(state2, B)
    assert context.getDependency(DummyState, B) == state2

    context.pushState(state3, A)
    assert context.getDependency(DummyState, A) == state3

    context.popState()
    assert context.getDependency(DummyState, A) == state1
    assert context.getDependency(DummyState, B) == state2

    context.popState()
    assert context.getDependency(DummyState, A) == state1
  }

  private class DummyState
          implements State
  {
  }
}
