/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.context

import com.google.inject.Binder
import com.google.inject.Module
import com.teradata.test.context.State
import spock.lang.Specification

import static com.google.inject.util.Modules.EMPTY_MODULE

class GuiceTestContextTest
        extends Specification
{
  private static final def A = 'A'
  private static final def B = 'B'
  private static final def C = 'C'

  def 'test get dependency'()
  {
    setup:
    def context = new GuiceTestContext(EMPTY_MODULE)
    def state = new DummyState()

    expect:
    context.pushStates(state)
    assert context.getDependency(DummyState) == state
  }

  def 'test override'()
  {
    setup:
    def state1 = new DummyState(A)
    def state2 = new DummyState(B)
    def context1 = new GuiceTestContext(new Module() {
      @Override
      void configure(Binder binder)
      {
        binder.bind(DummyState).toInstance(state1)
      }
    })
    context1.pushStates(state1)
    context1.pushStates(state2)
    context1.popStates()

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
    assert context2.getDependency(DummyState, A) == state1
  }

  def 'test states push/pop no naming'()
  {
    setup:
    def context = new GuiceTestContext(EMPTY_MODULE)
    def state1 = new DummyState()
    def state2 = new DummyState()

    expect:
    context.pushStates(state1)
    assert context.getDependency(DummyState) == state1

    context.pushStates(state2)
    assert context.getDependency(DummyState) == state2

    context.popStates()
    assert context.getDependency(DummyState) == state1
  }

  def 'test states push/pop external naming'()
  {
    setup:
    def context = new GuiceTestContext(EMPTY_MODULE)
    def state1 = new DummyState(A)
    def state2 = new DummyState(B)
    def state3 = new DummyState(C)
    def state4 = new DummyState(A)

    expect:
    context.pushStates(state1)
    assert context.getDependency(DummyState, A) == state1
    def obj1 = context.getDependency(DummyClass)

    context.pushStates(state2, state3)
    assert context.getDependency(DummyState, B) == state2
    assert context.getDependency(DummyState, C) == state3
    def obj2 = context.getDependency(DummyClass)

    context.pushStates(state4)
    assert context.getDependency(DummyState, A) == state4

    context.popStates()
    assert context.getDependency(DummyState, A) == state1
    assert context.getDependency(DummyState, B) == state2

    context.popStates()
    assert context.getDependency(DummyState, A) == state1

    context.popStates()
    def obj3 = context.getDependency(DummyClass)

    assert obj1 == obj3 != obj2
  }

  def 'test states push/pop internal naming'()
  {
    setup:
    def context = new GuiceTestContext(EMPTY_MODULE)
    def state1 = new DummyState(A)
    def state2 = new DummyState(B)
    def state3 = new DummyState(A)

    expect:
    context.pushStates(state1)
    assert context.getDependency(DummyState, A) == state1

    context.pushStates(state2)
    assert context.getDependency(DummyState, B) == state2

    context.pushStates(state3)
    assert context.getDependency(DummyState, A) == state3

    context.popStates()
    assert context.getDependency(DummyState, A) == state1
    assert context.getDependency(DummyState, B) == state2

    context.popStates()
    assert context.getDependency(DummyState, A) == state1
  }

  private class DummyState
          implements State
  {
    private final Optional<String> name;

    DummyState(String name = null)
    {
      this.name = Optional.ofNullable(name)
    }

    @Override
    Optional<String> getName()
    {
      return name
    }
  }

  private static class DummyClass
  {
  }
}
