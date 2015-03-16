/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static com.teradata.test.requirements.DataSourceRequirements.dataSource;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.requirements.TableRequirements.table;
import static org.testng.AssertJUnit.assertTrue;

class CommonTestRequirements
        implements RequirementsProvider
{
    @Override
    public Requirement getRequirements()
    {
        return compose(
                table("some table"),
                dataSource("some dataSource description")
        );
    }
}

@Requires(CommonTestRequirements.class)
public class ExampleTest
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ExampleTest.class);

    private static class RequirementSetOne
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements()
        {
            return compose(
                    table("some table"),
                    dataSource("some dataSource description")
            );
        }
    }

    @Requires(RequirementSetOne.class)
    @Test(groups = "example_smoketest")
    public void testOne()
    {
        assertTrue(true);
    }

    // @Requires(RequirementSetOne.class)
    @Test
    public void testTwo()
    {
        assertTrue(true);
    }

    private static class RequirementSetTwo
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements()
        {
            return compose(
                    table("some table"),
                    dataSource("some dataSource description")
            );
        }
    }

    @Requires({RequirementSetTwo.class, RequirementSetTwo.class})
    @Test(groups = "example_smoketest")
    public void testThree()
    {
        LOGGER.debug("logging in test");
        assertTrue(true);
    }
}
