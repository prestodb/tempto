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

package io.prestodb.tempto.context;

/**
 * Helper class used for execution of instances of {@link ContextRunnable} with context generated
 * by {@link ContextProvider#setup()}
 */
public final class ContextDsl
{
    public static <T> void executeWith(ContextProvider<T> provider, ContextRunnable<T> runnable)
    {
        T context = provider.setup();
        try {
            runnable.run(context);
        }
        finally {
            provider.cleanup(context);
        }
    }

    private ContextDsl()
    {
    }
}
