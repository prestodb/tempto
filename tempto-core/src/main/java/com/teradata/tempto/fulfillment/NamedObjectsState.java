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

package com.teradata.tempto.fulfillment;

import com.teradata.tempto.context.State;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class NamedObjectsState<T>
        implements State
{
    protected final Map<String, T> objects;
    protected final String objectDescription;

    public NamedObjectsState(Map<String, T> objects, String objectDescription)
    {
        this.objects = objects;
        this.objectDescription = objectDescription;
    }

    public T get(String name)
    {
        checkArgument(objects.containsKey(name), "no %s instance found for name %s", objectDescription, name);
        return objects.get(name);
    }
}
