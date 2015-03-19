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
import org.slf4j.Logger;

import java.util.Map;
import java.util.Stack;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.common.base.Preconditions.checkState;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Modules.combine;
import static org.slf4j.LoggerFactory.getLogger;

public class GuiceTestContext
        implements TestContext
{
    private final static Logger LOGGER = getLogger(GuiceTestContext.class);

    private final Module baseModule;
    private final Map<Key<State>, Stack<State>> stateStacks = newHashMap();
    private final Stack<PushStateDsl> pushedStateDsls = new Stack<>();
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

    private class PushStateDsl
            implements TestContext.PushStateDsl
    {
        Map<Key<State>, State> states = newHashMap();

        @Override
        public <S extends State> PushStateDsl pushState(S state)
        {
            if (state.getName().isPresent()) {
                return pushState(state, state.getName().get());
            }
            else {
                return pushState(Key.get((Class<State>) state.getClass()), state);
            }
        }

        private <S extends State> PushStateDsl pushState(S state, String stateName)
        {
            return pushState(Key.get((Class<State>) state.getClass(), named(stateName)), state);
        }

        private PushStateDsl pushState(Key<State> key, State state)
        {
            checkState(!states.containsKey(key));
            states.put(key, state);
            return this;
        }

        @Override
        public void finish()
        {
            LOGGER.debug("pushing states");
            for (Map.Entry<Key<State>, State> state : states.entrySet()) {
                Key key = state.getKey();

                if (!stateStacks.containsKey(key)) {
                    stateStacks.put(key, new Stack<>());
                }

                stateStacks.get(key).add(state.getValue());
                LOGGER.debug("key: {}, state: {}", key, state.getValue());
            }
            LOGGER.debug("pushing states finished");

            pushedStateDsls.push(this);
            pushInjector();
        }
    }

    @Override
    public PushStateDsl pushStates()
    {
        return new PushStateDsl();
    }

    @Override
    public void popStates()
    {
        PushStateDsl pushedStateDsl = pushedStateDsls.pop();

        LOGGER.debug("popping states");
        for (Key key : pushedStateDsl.states.keySet()) {
            State state = stateStacks.get(key).pop();
            LOGGER.debug("key: {}, state: {}", key, state);
        }
        LOGGER.debug("popping states finished");

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
