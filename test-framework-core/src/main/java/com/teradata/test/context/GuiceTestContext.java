/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import java.util.Map;
import java.util.Stack;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Modules.combine;

public class GuiceTestContext
        implements TestContext
{
    private final Module baseModule;
    private final Map<Key<State>, Stack<State>> stateStacks = newHashMap();
    private final Stack<Key> pushedStates = new Stack<>();
    private final Stack<Injector> injectors = new Stack<>();

    public GuiceTestContext(Module... baseModules)
    {
        this.baseModule = combine(baseModules);
        pushInjector();
    }

    private GuiceTestContext(GuiceTestContext other, Module... overrideModules)
    {
        this.baseModule = Modules.override(other.baseModule).with(overrideModules);
        for (Map.Entry<Key<State>, Stack<State>> otherStateStack : other.stateStacks.entrySet()) {
            if (otherStateStack.getValue().isEmpty()) {
                continue;
            }

            Stack<State> stateStack = new Stack<>();
            stateStack.push(otherStateStack.getValue().peek());
            this.stateStacks.put(otherStateStack.getKey(), stateStack);
        }
        pushInjector();
    }

    @Override
    public <T> T getDependency(Class<T> dependencyClass)
    {
        return getDependency(Key.get(dependencyClass));
    }

    @Override
    public <T> T getDependency(Class<T> dependencyClass, String dependencyName)
    {
        return getDependency(Key.get(dependencyClass, named(dependencyName)));
    }

    private <T> T getDependency(Key key)
    {
        return (T) injectors.peek().getInstance(key);
    }

    @Override
    public <S extends State> void pushState(S state)
    {
        if (state.getName().isPresent()) {
            pushState(state, state.getName().get());
        } else {
            pushState(Key.get((Class<State>) state.getClass()), state);
        }
    }

    @Override
    public <S extends State> void pushState(S state, String stateName)
    {
        pushState(Key.get((Class<State>) state.getClass(), named(stateName)), state);
    }

    private void pushState(Key<State> key, State state)
    {
        if (!stateStacks.containsKey(key)) {
            stateStacks.put(key, new Stack<>());
        }

        stateStacks.get(key).add(state);
        pushedStates.push(key);
        pushInjector();
    }

    @Override
    public void popState()
    {
        stateStacks.get(pushedStates.pop()).pop();
        injectors.pop();
    }

    public void close()
    {
        // TODO: notify listeners
    }

    public GuiceTestContext override(Module... overrideModules)
    {
        return new GuiceTestContext(this, overrideModules);
    }

    private void pushInjector()
    {
        injectors.push(Guice.createInjector(combine(baseModule, statesModule(), testContextModule())));
    }

    private Module statesModule()
    {
        return (Binder binder) -> {
            for (Map.Entry<Key<State>, Stack<State>> stateStack : stateStacks.entrySet()) {
                if (stateStack.getValue().isEmpty()) {
                    continue;
                }

                binder.bind(stateStack.getKey()).toInstance(stateStack.getValue().peek());
            }
        };
    }

    private Module testContextModule()
    {
        return (Binder binder) -> {
            binder.bind(TestContext.class).toInstance(this);
        };
    }
}
