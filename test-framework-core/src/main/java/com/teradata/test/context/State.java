/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.context;

import java.util.Optional;

/**
 * Marker interface of state objects produced by fulfillers.
 */
public interface State
{

    /**
     * Return name for state. If non-empty optional is
     * returned State will be bound in TestContext with name annotation.
     */
    default Optional<String> getName() {
        return Optional.empty();
    }
}
