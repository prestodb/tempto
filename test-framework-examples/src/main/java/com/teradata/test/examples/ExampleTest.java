/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.teradata.test.Requirement;
import com.teradata.test.RequirementsProvider;
import com.teradata.test.Requires;
import org.testng.annotations.Test;

import static com.teradata.test.DataSourceRequirements.dataSource;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.TableRequirements.table;
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
    @Test(groups = "example-smoketest")
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
    @Test(groups = "example-smoketest")
    public void testThree()
    {
        assertTrue(true);
    }
}
