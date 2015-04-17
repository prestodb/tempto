/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment;

import com.teradata.test.context.State;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class NamedObjectsState<T>
        implements State
{
    private final Map<String, T> objects;
    private String objectDescription;

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
