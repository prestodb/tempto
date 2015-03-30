/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.context;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.teradata.test.context.State;
import com.teradata.test.context.TestContext;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Modules.combine;
import static org.slf4j.LoggerFactory.getLogger;

public class GuiceTestContext
        implements TestContext
{
    private final static Logger LOGGER = getLogger(GuiceTestContext.class);

    private final Module baseModule;
    private final Map<Key<State>, Stack<State>> stateStacks = newHashMap();
    private final Stack<Map<Key<State>, State>> pushedStateMaps = new Stack<>();
    private final Stack<Injector> injectors = new Stack<>();
    private final List<Runnable> closeCallbacks = newArrayList();

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
    public void pushStates(Iterable<State> states)
    {
        Map<Key<State>, State> stateMap = newHashMap();
        for (State state : states) {
            stateMap.put(getKeyFor(state), state);
        }

        LOGGER.debug("pushing states");
        for (Map.Entry<Key<State>, State> stateEntry : stateMap.entrySet()) {
            pushState(stateEntry.getKey(), stateEntry.getValue());
        }
        LOGGER.debug("pushing states finished");

        pushedStateMaps.push(stateMap);
        pushInjector();
    }

    private void pushState(Key<State> key, State state)
    {
        if (!stateStacks.containsKey(key)) {
            stateStacks.put(key, new Stack<>());
        }

        stateStacks.get(key).add(state);
        LOGGER.debug("key: {}, state: {}", key, state);
    }

    private Key<State> getKeyFor(State state)
    {
        if (state.getName().isPresent()) {
            return Key.get((Class<State>) state.getClass(), named(state.getName().get()));
        }
        else {
            return Key.get((Class<State>) state.getClass());
        }
    }

    @Override
    public void popStates()
    {
        Map<Key<State>, State> stateMap = pushedStateMaps.pop();

        LOGGER.debug("popping states");
        for (Key key : stateMap.keySet()) {
            State state = stateStacks.get(key).pop();
            LOGGER.debug("key: {}, state: {}", key, state);
        }
        LOGGER.debug("popping states finished");

        injectors.pop();
    }

    @Override
    public void registerCloseCallback(Runnable callback)
    {
        closeCallbacks.add(callback);
    }

    @Override
    public void close()
    {
        closeCallbacks.forEach(java.lang.Runnable::run);
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
