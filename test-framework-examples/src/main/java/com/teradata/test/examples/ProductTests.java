/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.initialization.TestInitializationListener;
import com.teradata.test.requirements.TableRequirements;
import org.testng.annotations.Listeners;

@Listeners(TestInitializationListener.class)
public class ProductTests
{

    public static class BaseProductTestRequirements
            implements RequirementsProvider
    {

        @Override
        public Requirement getRequirements()
        {
            return TableRequirements.table("dummy table");
        }
    }
}
