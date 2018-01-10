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

package io.prestodb.tempto.util;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@ThreadSafe
public class Lazy<T>
        implements Provider<T>
{
    private final Provider<T> provider;
    private T instance;

    public Lazy(Provider<T> provider)
    {
        this.provider = requireNonNull(provider, "provider is null");
    }

    @Override
    public synchronized T get()
    {
        if (instance == null) {
            instance = requireNonNull(provider.get());
        }
        return instance;
    }

    public synchronized Optional<T> lazyGet()
    {
        return Optional.ofNullable(instance);
    }
}
