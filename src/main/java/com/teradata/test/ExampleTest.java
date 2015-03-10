package com.teradata.test;


import org.testng.annotations.Test;

import static com.teradata.test.DataSourceRequirements.dataSource;
import static com.teradata.test.Requirements.compose;
import static com.teradata.test.TableRequirements.table;

class CommonTestRequirements implements RequirementsProvider {
    @Override
    public Requirement getRequirements() {
        return compose(
                table("some table"),
                dataSource("some dataSource description")
        );
    }
}

@Requires(CommonTestRequirements.class)
public class ExampleTest {

    private static class RequirementSetOne implements RequirementsProvider {
        @Override
        public Requirement getRequirements() {
            return compose(
                    table("some table"),
                    dataSource("some dataSource description")
            );
        }
    }

    @Requires(RequirementSetOne.class)
    @Test(groups = "g1")
    void testOne() {

    }

    @Requires(RequirementSetOne.class)
    @Test(groups = "g1")
    void testTwo() {

    }

    private static class RequirementSetTwo implements RequirementsProvider {
        @Override
        public Requirement getRequirements() {
            return compose(
                    table("some table"),
                    dataSource("some dataSource description")
            );
        }
    }

    @Requires({RequirementSetTwo.class, RequirementSetTwo.class})
    @Test(groups = "g1")
    void testThree() {

    }

}
