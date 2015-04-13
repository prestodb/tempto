/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test;

import com.teradata.test.internal.initialization.TestInitializationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners({TestInitializationListener.class})
public class ProductTest
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(ProductTest.class);
}
