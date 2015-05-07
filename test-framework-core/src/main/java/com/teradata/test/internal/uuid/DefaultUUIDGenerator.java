/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.uuid;

import java.util.UUID;

public class DefaultUUIDGenerator
        implements UUIDGenerator
{
    @Override
    public String randomUUID()
    {
        return UUID.randomUUID().toString();
    }
}
