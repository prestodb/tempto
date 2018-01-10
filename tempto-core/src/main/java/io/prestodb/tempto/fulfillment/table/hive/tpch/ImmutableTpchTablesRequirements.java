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

package io.prestodb.tempto.fulfillment.table.hive.tpch;

import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.Requirements;
import io.prestodb.tempto.RequirementsProvider;
import io.prestodb.tempto.configuration.Configuration;

import static io.prestodb.tempto.fulfillment.table.TableRequirements.immutableTable;

public class ImmutableTpchTablesRequirements
        implements RequirementsProvider
{
    public static final Requirement PART = immutableTable(TpchTableDefinitions.PART);
    public static final Requirement NATION = immutableTable(TpchTableDefinitions.NATION);
    public static final Requirement REGION = immutableTable(TpchTableDefinitions.REGION);
    public static final Requirement ORDERS = immutableTable(TpchTableDefinitions.ORDERS);
    public static final Requirement SUPPLIER = immutableTable(TpchTableDefinitions.SUPPLIER);
    public static final Requirement CUSTOMER = immutableTable(TpchTableDefinitions.CUSTOMER);
    public static final Requirement LINE_ITEM = immutableTable(TpchTableDefinitions.LINE_ITEM);
    public static final Requirement PART_SUPPLIER = immutableTable(TpchTableDefinitions.PART_SUPPLIER);

    public static class ImmutablePartTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return PART;
        }
    }

    public static class ImmutableNationTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return NATION;
        }
    }

    public static class ImmutableRegionTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return REGION;
        }
    }

    public static class ImmutableOrdersTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return ORDERS;
        }
    }

    public static class ImmutableSupplierTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return SUPPLIER;
        }
    }

    public static class ImmutableCustomerTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return CUSTOMER;
        }
    }

    public static class ImmutableLineItemTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return LINE_ITEM;
        }
    }

    public static class ImmutablePartSupplierTable
            implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements(Configuration configuration)
        {
            return PART_SUPPLIER;
        }
    }

    @Override
    public Requirement getRequirements(Configuration configuration)
    {
        return Requirements.compose(
                CUSTOMER,
                NATION,
                LINE_ITEM,
                ORDERS,
                PART,
                PART_SUPPLIER,
                SUPPLIER,
                REGION);
    }
}
