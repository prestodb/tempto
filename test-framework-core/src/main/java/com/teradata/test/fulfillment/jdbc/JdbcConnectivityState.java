/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.jdbc;

import com.teradata.test.context.State;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Contains configuration required to connect to a JDBC server.
 */
public class JdbcConnectivityState
        implements State
{
    public final String driverClass;
    public final String url;
    public final String user;
    public final String password;

    public JdbcConnectivityState(String driverClass, String url, String user, String password)
    {
        this.driverClass = driverClass;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public boolean equals(Object o)
    {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }
}
