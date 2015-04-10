/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.context;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.teradata.test.context.State;
import com.teradata.test.context.TestContext;
import com.teradata.test.context.TestContextCloseCallback;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static com.google.inject.util.Modules.combine;
import static com.google.inject.util.Modules.override;
import static java.util.Collections.synchronizedList;
import static org.assertj.core.util.Lists.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

public class GuiceTestContext
        implements TestContext
{
    private final static Logger LOGGER = getLogger(GuiceTestContext.class);

    private final Optional<GuiceTestContext> parent;
    private final List<GuiceTestContext> children = synchronizedList(newArrayList());
    private final Module baseModule;
    private final Map<Key<State>, State> states;
    private final Injector injector;
    private final List<TestContextCloseCallback> closeCallbacks = newArrayList();

    public GuiceTestContext(Module... baseModules)
    {
        this(Optional.<GuiceTestContext>empty(), combine(baseModules), newHashMap());
    }

    private GuiceTestContext(Optional<GuiceTestContext> parent, Module baseModule, Map<Key<State>, State> states)
    {
        this.parent = parent;
        this.baseModule = baseModule;
        this.states = states;
        this.injector = buildInjector();
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
        return (T) injector.getInstance(key);
    }

    @Override
    public GuiceTestContext createChildContext(Iterable<State> newStatesIterable)
    {
        return createChildContext(newStatesIterable, new Module[] {});
    }

    public GuiceTestContext createChildContext(Module... overrideModules)
    {
        return createChildContext(emptyList(), overrideModules);
    }

    public GuiceTestContext createChildContext(Iterable<State> newStatesIterable, Module... overrideModules)
    {
        LOGGER.debug("Creating new test context from " + this);

        Map<Key<State>, State> newStates = newHashMap(states);
        for (State newState : newStatesIterable) {
            newStates.put(getKeyFor(newState), newState);
        }

        GuiceTestContext childTestContext = new GuiceTestContext(Optional.of(this), override(baseModule).with(overrideModules), newStates);
        children.add(childTestContext);
        return childTestContext;
    }

    @Override
    public void registerCloseCallback(TestContextCloseCallback callback)
    {
        closeCallbacks.add(callback);
    }

    @Override
    public void close()
    {
        copyOf(children).forEach(GuiceTestContext::close);
        closeCallbacks.forEach(callback -> callback.testContextClosed(this));

        if (parent.isPresent()) {
            parent.get().children.remove(this);
        }
    }

    public void injectMembers(Object instance)
    {
        injector.injectMembers(instance);
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

    private Injector buildInjector()
    {
        return createInjector(combine(baseModule, statesModule(), testContextModule()));
    }

    private Module statesModule()
    {
        return (Binder binder) -> {
            for (Map.Entry<Key<State>, State> stateEntry : states.entrySet()) {
                binder.bind(stateEntry.getKey()).toInstance(stateEntry.getValue());
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
