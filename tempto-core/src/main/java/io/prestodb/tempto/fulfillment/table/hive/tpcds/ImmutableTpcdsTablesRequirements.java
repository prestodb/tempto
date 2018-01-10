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

package io.prestodb.tempto.fulfillment.table.hive.tpcds;

import io.prestodb.tempto.Requirement;
import io.prestodb.tempto.Requirements;
import io.prestodb.tempto.RequirementsProvider;
import io.prestodb.tempto.configuration.Configuration;

import static io.prestodb.tempto.fulfillment.table.TableRequirements.immutableTable;

public class ImmutableTpcdsTablesRequirements
        implements RequirementsProvider
{
    public static final Requirement CATALOG_SALES = immutableTable(TpcdsTableDefinitions.CATALOG_SALES);
    public static final Requirement CALL_CENTER = immutableTable(TpcdsTableDefinitions.CALL_CENTER);
    public static final Requirement CATALOG_PAGE = immutableTable(TpcdsTableDefinitions.CATALOG_PAGE);
    public static final Requirement CATALOG_RETURNS = immutableTable(TpcdsTableDefinitions.CATALOG_RETURNS);
    public static final Requirement CUSTOMER = immutableTable(TpcdsTableDefinitions.CUSTOMER);
    public static final Requirement CUSTOMER_ADDRESS = immutableTable(TpcdsTableDefinitions.CUSTOMER_ADDRESS);
    public static final Requirement CUSTOMER_DEMOGRAPHICS = immutableTable(TpcdsTableDefinitions.CUSTOMER_DEMOGRAPHICS);
    public static final Requirement DATE_DIM = immutableTable(TpcdsTableDefinitions.DATE_DIM);
    public static final Requirement HOUSEHOLD_DEMOGRAPHICS = immutableTable(TpcdsTableDefinitions.HOUSEHOLD_DEMOGRAPHICS);
    public static final Requirement INCOME_BAND = immutableTable(TpcdsTableDefinitions.INCOME_BAND);
    public static final Requirement INVENTORY = immutableTable(TpcdsTableDefinitions.INVENTORY);
    public static final Requirement ITEM = immutableTable(TpcdsTableDefinitions.ITEM);
    public static final Requirement PROMOTION = immutableTable(TpcdsTableDefinitions.PROMOTION);
    public static final Requirement REASON = immutableTable(TpcdsTableDefinitions.REASON);
    public static final Requirement SHIP_MODE = immutableTable(TpcdsTableDefinitions.SHIP_MODE);
    public static final Requirement STORE = immutableTable(TpcdsTableDefinitions.STORE);
    public static final Requirement STORE_RETURNS = immutableTable(TpcdsTableDefinitions.STORE_RETURNS);
    public static final Requirement STORE_SALES = immutableTable(TpcdsTableDefinitions.STORE_SALES);
    public static final Requirement TIME_DIM = immutableTable(TpcdsTableDefinitions.TIME_DIM);
    public static final Requirement WAREHOUSE = immutableTable(TpcdsTableDefinitions.WAREHOUSE);
    public static final Requirement WEB_PAGE = immutableTable(TpcdsTableDefinitions.WEB_PAGE);
    public static final Requirement WEB_RETURNS = immutableTable(TpcdsTableDefinitions.WEB_RETURNS);
    public static final Requirement WEB_SALES = immutableTable(TpcdsTableDefinitions.WEB_SALES);
    public static final Requirement WEB_SITE = immutableTable(TpcdsTableDefinitions.WEB_SITE);

    @Override
    public Requirement getRequirements(Configuration configuration)
    {
        return Requirements.compose(
                CATALOG_SALES,
                CALL_CENTER,
                CATALOG_PAGE,
                CATALOG_RETURNS,
                CUSTOMER,
                CUSTOMER_ADDRESS,
                CUSTOMER_DEMOGRAPHICS,
                DATE_DIM,
                HOUSEHOLD_DEMOGRAPHICS,
                INCOME_BAND,
                INVENTORY,
                ITEM,
                PROMOTION,
                REASON,
                SHIP_MODE,
                STORE,
                STORE_RETURNS,
                STORE_SALES,
                TIME_DIM,
                WAREHOUSE,
                WEB_PAGE,
                WEB_RETURNS,
                WEB_SALES,
                WEB_SITE);
    }
}
