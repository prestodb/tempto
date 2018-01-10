/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.internal.context;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import io.prestodb.tempto.context.State;
import io.prestodb.tempto.context.TestContext;
import io.prestodb.tempto.context.TestContextCloseCallback;
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
    public <T> Optional<T> getOptionalDependency(Class<T> dependencyClass)
    {
        return getOptionalDependency(Key.get(dependencyClass));
    }

    @Override
    public <T> Optional<T> getOptionalDependency(Class<T> dependencyClass, String dependencyName)
    {
        return getOptionalDependency(Key.get(dependencyClass, named(dependencyName)));
    }

    public <T> Optional<T> getOptionalDependency(Key key)
    {
        if (injector.getExistingBinding(key) != null) {
            return Optional.of(getDependency(key));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public GuiceTestContext createChildContext(Iterable<State> newStatesIterable)
    {
        return createChildContext(newStatesIterable, emptyList());
    }

    public GuiceTestContext createChildContext(Iterable<State> newStatesIterable, Iterable<Module> overrideModules)
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
        Lists.reverse(closeCallbacks).forEach(callback -> callback.testContextClosed(this));

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

    public Injector getInjector()
    {
        return injector;
    }
}
