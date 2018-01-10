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

import com.teradata.tpcds.Table;

/**
 * Enum containing names of all TPCDS tables. Moreover it holds reference to
 * {@link com.teradata.tpcds.Table} entity which is used for generating data.
 */
public enum TpcdsTable
{
    CATALOG_SALES(Table.CATALOG_SALES),
    CALL_CENTER(Table.CALL_CENTER),
    CATALOG_PAGE(Table.CATALOG_PAGE),
    CATALOG_RETURNS(Table.CATALOG_RETURNS),
    CUSTOMER(Table.CUSTOMER),
    CUSTOMER_ADDRESS(Table.CUSTOMER_ADDRESS),
    CUSTOMER_DEMOGRAPHICS(Table.CUSTOMER_DEMOGRAPHICS),
    DATE_DIM(Table.DATE_DIM),
    HOUSEHOLD_DEMOGRAPHICS(Table.HOUSEHOLD_DEMOGRAPHICS),
    INCOME_BAND(Table.INCOME_BAND),
    INVENTORY(Table.INVENTORY),
    ITEM(Table.ITEM),
    PROMOTION(Table.PROMOTION),
    REASON(Table.REASON),
    SHIP_MODE(Table.SHIP_MODE),
    STORE(Table.STORE),
    STORE_RETURNS(Table.STORE_RETURNS),
    STORE_SALES(Table.STORE_SALES),
    TIME_DIM(Table.TIME_DIM),
    WAREHOUSE(Table.WAREHOUSE),
    WEB_PAGE(Table.WEB_PAGE),
    WEB_RETURNS(Table.WEB_RETURNS),
    WEB_SALES(Table.WEB_SALES),
    WEB_SITE(Table.WEB_SITE);

    private final Table table;

    TpcdsTable(Table table)
    {
        this.table = table;
    }

    public Table getTable()
    {
        return table;
    }
}
