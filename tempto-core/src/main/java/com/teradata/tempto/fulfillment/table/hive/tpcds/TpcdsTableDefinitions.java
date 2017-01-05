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

package com.teradata.tempto.fulfillment.table.hive.tpcds;

import com.teradata.tempto.fulfillment.table.TableDefinitionsRepository.RepositoryTableDefinition;
import com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition;

// Table definitions according to: tpc.org/tpc_documents_current_versions/pdf/tpc-ds_v2.3.0.pdf
// TODO: move to separate module
public class TpcdsTableDefinitions
{
    @RepositoryTableDefinition
    public static final HiveTableDefinition CALL_CENTER =
            HiveTableDefinition.builder("call_center")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    cc_call_center_sk BIGINT," +
                            "    cc_call_center_id CHAR(16)," +
                            "    cc_rec_start_date DATE," +
                            "    cc_rec_end_date DATE," +
                            "    cc_closed_date_sk INT," +
                            "    cc_open_date_sk INT," +
                            "    cc_name VARCHAR(50)," +
                            "    cc_class VARCHAR(50)," +
                            "    cc_employees INT," +
                            "    cc_sq_ft INT," +
                            "    cc_hours CHAR(20)," +
                            "    cc_manager VARCHAR(40)," +
                            "    cc_mkt_id INT," +
                            "    cc_mkt_class CHAR(50)," +
                            "    cc_mkt_desc VARCHAR(1)," +
                            "    cc_market_manager VARCHAR(40)," +
                            "    cc_division INT," +
                            "    cc_division_name VARCHAR(50)," +
                            "    cc_company INT," +
                            "    cc_company_name CHAR(50)," +
                            "    cc_street_number CHAR(10)," +
                            "    cc_street_name VARCHAR(60)," +
                            "    cc_street_type CHAR(15)," +
                            "    cc_suite_number CHAR(10)," +
                            "    cc_city VARCHAR(60)," +
                            "    cc_county VARCHAR(30)," +
                            "    cc_state CHAR(2)," +
                            "    cc_zip CHAR(10)," +
                            "    cc_country VARCHAR(20)," +
                            "    cc_gmt_offset DECIMAL(5,2)," +
                            "    cc_tax_percentage DECIMAL(5,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.CALL_CENTER, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CATALOG_PAGE =
            HiveTableDefinition.builder("catalog_page")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    cp_catalog_page_sk BIGINT," +
                            "    cp_catalog_page_id CHAR(16)," +
                            "    cp_start_date_sk INT," +
                            "    cp_end_date_sk INT," +
                            "    cp_department VARCHAR(50)," +
                            "    cp_catalog_number INT," +
                            "    cp_catalog_page_number INT," +
                            "    cp_description VARCHAR(1)," +
                            "    cp_type VARCHAR(1))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.CATALOG_PAGE, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CATALOG_RETURNS =
            HiveTableDefinition.builder("catalog_returns")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    cr_returned_date_sk BIGINT," +
                            "    cr_returned_time_sk BIGINT," +
                            "    cr_item_sk BIGINT," +
                            "    cr_refunded_customer_sk BIGINT," +
                            "    cr_refunded_cdemo_sk BIGINT," +
                            "    cr_refunded_hdemo_sk BIGINT," +
                            "    cr_refunded_addr_sk BIGINT," +
                            "    cr_returning_customer_sk BIGINT," +
                            "    cr_returning_cdemo_sk BIGINT," +
                            "    cr_returning_hdemo_sk BIGINT," +
                            "    cr_returning_addr_sk BIGINT," +
                            "    cr_call_center_sk BIGINT," +
                            "    cr_catalog_page_sk BIGINT," +
                            "    cr_ship_mode_sk BIGINT," +
                            "    cr_warehouse_sk BIGINT," +
                            "    cr_reason_sk BIGINT," +
                            "    cr_order_number BIGINT," +
                            "    cr_return_quantity INT," +
                            "    cr_return_amount DECIMAL(7,2)," +
                            "    cr_return_tax DECIMAL(7,2)," +
                            "    cr_return_amt_inc_tax DECIMAL(7,2)," +
                            "    cr_fee DECIMAL(7,2)," +
                            "    cr_return_ship_cost DECIMAL(7,2)," +
                            "    cr_refunded_cash DECIMAL(7,2)," +
                            "    cr_reversed_charge DECIMAL(7,2)," +
                            "    cr_store_credit DECIMAL(7,2)," +
                            "    cr_net_loss DECIMAL(7,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.CATALOG_RETURNS, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CATALOG_SALES =
            HiveTableDefinition.builder("catalog_sales")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    cs_sold_date_sk BIGINT," +
                            "    cs_sold_time_sk BIGINT," +
                            "    cs_ship_date_sk BIGINT," +
                            "    cs_bill_customer_sk BIGINT," +
                            "    cs_bill_cdemo_sk BIGINT," +
                            "    cs_bill_hdemo_sk BIGINT," +
                            "    cs_bill_addr_sk BIGINT," +
                            "    cs_ship_customer_sk BIGINT," +
                            "    cs_ship_cdemo_sk BIGINT," +
                            "    cs_ship_hdemo_sk BIGINT," +
                            "    cs_ship_addr_sk BIGINT," +
                            "    cs_call_center_sk BIGINT," +
                            "    cs_catalog_page_sk BIGINT," +
                            "    cs_ship_mode_sk BIGINT," +
                            "    cs_warehouse_sk BIGINT," +
                            "    cs_item_sk BIGINT," +
                            "    cs_promo_sk BIGINT," +
                            "    cs_order_number BIGINT," +
                            "    cs_quantity INT," +
                            "    cs_wholesale_cost DECIMAL(7,2)," +
                            "    cs_list_price DECIMAL(7,2)," +
                            "    cs_sales_price DECIMAL(7,2)," +
                            "    cs_ext_discount_amt DECIMAL(7,2)," +
                            "    cs_ext_sales_price DECIMAL(7,2)," +
                            "    cs_ext_wholesale_cost DECIMAL(7,2)," +
                            "    cs_ext_list_price DECIMAL(7,2)," +
                            "    cs_ext_tax DECIMAL(7,2)," +
                            "    cs_coupon_amt DECIMAL(7,2)," +
                            "    cs_ext_ship_cost DECIMAL(7,2)," +
                            "    cs_net_paid DECIMAL(7,2)," +
                            "    cs_net_paid_inc_tax DECIMAL(7,2)," +
                            "    cs_net_paid_inc_ship DECIMAL(7,2)," +
                            "    cs_net_paid_inc_ship_tax DECIMAL(7,2)," +
                            "    cs_net_profit DECIMAL(7,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.CATALOG_SALES, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CUSTOMER =
            HiveTableDefinition.builder("customer")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    c_customer_sk BIGINT," +
                            "    c_customer_id CHAR(16)," +
                            "    c_current_cdemo_sk BIGINT," +
                            "    c_current_hdemo_sk BIGINT," +
                            "    c_current_addr_sk BIGINT," +
                            "    c_first_shipto_date_sk BIGINT," +
                            "    c_first_sales_date_sk BIGINT," +
                            "    c_salutation CHAR(10)," +
                            "    c_first_name CHAR(20)," +
                            "    c_last_name CHAR(30)," +
                            "    c_preferred_cust_flag CHAR(1)," +
                            "    c_birth_day INT," +
                            "    c_birth_month INT," +
                            "    c_birth_year INT," +
                            "    c_birth_country VARCHAR(20)," +
                            "    c_login CHAR(13)," +
                            "    c_email_address CHAR(50)," +
                            "    c_last_review_date_sk BIGINT)" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.CUSTOMER, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CUSTOMER_ADDRESS =
            HiveTableDefinition.builder("customer_address")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    ca_address_sk BIGINT," +
                            "    ca_address_id CHAR(16)," +
                            "    ca_street_number CHAR(10)," +
                            "    ca_street_name VARCHAR(60)," +
                            "    ca_street_type CHAR(15)," +
                            "    ca_suite_number CHAR(10)," +
                            "    ca_city VARCHAR(60)," +
                            "    ca_county VARCHAR(30)," +
                            "    ca_state CHAR(2)," +
                            "    ca_zip CHAR(10)," +
                            "    ca_country VARCHAR(20)," +
                            "    ca_gmt_offset DECIMAL(5,2)," +
                            "    ca_location_type CHAR(20))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.CUSTOMER_ADDRESS, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition CUSTOMER_DEMOGRAPHICS =
            HiveTableDefinition.builder("customer_demographics")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    cd_demo_sk BIGINT," +
                            "    cd_gender CHAR(1)," +
                            "    cd_marital_status CHAR(1)," +
                            "    cd_education_status CHAR(20)," +
                            "    cd_purchase_estimate INT," +
                            "    cd_credit_rating CHAR(10)," +
                            "    cd_dep_count INT," +
                            "    cd_dep_employed_count INT," +
                            "    cd_dep_college_count INT)" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.CUSTOMER_DEMOGRAPHICS, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition DATE_DIM =
            HiveTableDefinition.builder("date_dim")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    d_date_sk BIGINT," +
                            "    d_date_id CHAR(16)," +
                            "    d_date DATE," +
                            "    d_month_seq INT," +
                            "    d_week_seq INT," +
                            "    d_quarter_seq INT," +
                            "    d_year INT," +
                            "    d_dow INT," +
                            "    d_moy INT," +
                            "    d_dom INT," +
                            "    d_qoy INT," +
                            "    d_fy_year INT," +
                            "    d_fy_quarter_seq INT," +
                            "    d_fy_week_seq INT," +
                            "    d_day_name CHAR(9)," +
                            "    d_quarter_name CHAR(6)," +
                            "    d_holiday CHAR(1)," +
                            "    d_weekend CHAR(1)," +
                            "    d_following_holiday CHAR(1)," +
                            "    d_first_dom INT," +
                            "    d_last_dom INT," +
                            "    d_same_day_ly INT," +
                            "    d_same_day_lq INT," +
                            "    d_current_day CHAR(1)," +
                            "    d_current_week CHAR(1)," +
                            "    d_current_month CHAR(1)," +
                            "    d_current_quarter CHAR(1)," +
                            "    d_current_year CHAR(1))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.DATE_DIM, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition HOUSEHOLD_DEMOGRAPHICS =
            HiveTableDefinition.builder("household_demographics")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    hd_demo_sk BIGINT," +
                            "    hd_income_band_sk BIGINT," +
                            "    hd_buy_potential CHAR(15)," +
                            "    hd_dep_count INT," +
                            "    hd_vehicle_count INT)" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.HOUSEHOLD_DEMOGRAPHICS, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition INCOME_BAND =
            HiveTableDefinition.builder("income_band")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    ib_income_band_sk BIGINT," +
                            "    ib_lower_bound INT," +
                            "    ib_upper_bound INT)" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.INCOME_BAND, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition INVENTORY =
            HiveTableDefinition.builder("inventory")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    inv_date_sk BIGINT," +
                            "    inv_item_sk BIGINT," +
                            "    inv_warehouse_sk BIGINT," +
                            "    inv_quantity_on_hand INT)" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.INVENTORY, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition ITEM =
            HiveTableDefinition.builder("item")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    i_item_sk BIGINT," +
                            "    i_item_id CHAR(16)," +
                            "    i_rec_start_date DATE," +
                            "    i_rec_end_date DATE," +
                            "    i_item_desc VARCHAR(200)," +
                            "    i_current_price DECIMAL(7,2)," +
                            "    i_wholesale_cost DECIMAL(7,2)," +
                            "    i_brand_id INT," +
                            "    i_brand CHAR(50)," +
                            "    i_class_id INT," +
                            "    i_class CHAR(50)," +
                            "    i_category_id INT," +
                            "    i_category CHAR(50)," +
                            "    i_manufact_id INT," +
                            "    i_manufact CHAR(50)," +
                            "    i_size CHAR(20)," +
                            "    i_formulation CHAR(20)," +
                            "    i_color CHAR(20)," +
                            "    i_units CHAR(10)," +
                            "    i_container CHAR(10)," +
                            "    i_manager_id INT," +
                            "    i_product_name CHAR(50))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.ITEM, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition PROMOTION =
            HiveTableDefinition.builder("promotion")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    p_promo_sk BIGINT," +
                            "    p_promo_id CHAR(16)," +
                            "    p_start_date_sk BIGINT," +
                            "    p_end_date_sk BIGINT," +
                            "    p_item_sk BIGINT," +
                            "    p_cost DECIMAL(15,2)," +
                            "    p_response_targe INT," +
                            "    p_promo_name VARCHAR(50)," +
                            "    p_channel_dmail CHAR(1)," +
                            "    p_channel_email CHAR(1)," +
                            "    p_channel_catalog CHAR(1)," +
                            "    p_channel_tv CHAR(1)," +
                            "    p_channel_radio CHAR(1)," +
                            "    p_channel_press CHAR(1)," +
                            "    p_channel_event CHAR(1)," +
                            "    p_channel_demo CHAR(1)," +
                            "    p_channel_details VARCHAR(1)," +
                            "    p_purpose CHAR(15)," +
                            "    p_discount_active CHAR(1))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.PROMOTION, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition REASON =
            HiveTableDefinition.builder("reason")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    r_reason_sk BIGINT," +
                            "    r_reason_id CHAR(16)," +
                            "    r_reason_desc CHAR(100))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.REASON, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition SHIP_MODE =
            HiveTableDefinition.builder("ship_mode")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    sm_ship_mode_sk BIGINT," +
                            "    sm_ship_mode_id CHAR(16)," +
                            "    sm_type CHAR(30)," +
                            "    sm_code CHAR(10)," +
                            "    sm_carrier CHAR(20)," +
                            "    sm_contract CHAR(20))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.SHIP_MODE, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition STORE =
            HiveTableDefinition.builder("store")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    s_store_sk BIGINT," +
                            "    s_store_id CHAR(16)," +
                            "    s_rec_start_date DATE," +
                            "    s_rec_end_date DATE," +
                            "    s_closed_date_sk BIGINT," +
                            "    s_store_name VARCHAR(50)," +
                            "    s_number_employees INT," +
                            "    s_floor_space INT," +
                            "    s_hours CHAR(20)," +
                            "    s_manager VARCHAR(40)," +
                            "    s_market_id INT," +
                            "    s_geography_class VARCHAR(1)," +
                            "    s_market_desc VARCHAR(1)," +
                            "    s_market_manager VARCHAR(40)," +
                            "    s_division_id INT," +
                            "    s_division_name VARCHAR(50)," +
                            "    s_company_id INT," +
                            "    s_company_name VARCHAR(50)," +
                            "    s_street_number VARCHAR(10)," +
                            "    s_street_name VARCHAR(60)," +
                            "    s_street_type CHAR(15)," +
                            "    s_suite_number CHAR(10)," +
                            "    s_city VARCHAR(60)," +
                            "    s_county VARCHAR(30)," +
                            "    s_state CHAR(2)," +
                            "    s_zip CHAR(10)," +
                            "    s_country VARCHAR(20)," +
                            "    s_gmt_offset DECIMAL(5,2)," +
                            "    s_tax_precentage DECIMAL(5,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.STORE, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition STORE_RETURNS =
            HiveTableDefinition.builder("store_returns")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    sr_returned_date_sk BIGINT," +
                            "    sr_return_time_sk BIGINT," +
                            "    sr_item_sk BIGINT," +
                            "    sr_customer_sk BIGINT," +
                            "    sr_cdemo_sk BIGINT," +
                            "    sr_hdemo_sk BIGINT," +
                            "    sr_addr_sk BIGINT," +
                            "    sr_store_sk BIGINT," +
                            "    sr_reason_sk BIGINT," +
                            "    sr_ticket_number BIGINT," +
                            "    sr_return_quantity INT," +
                            "    sr_return_amt DECIMAL(7,2)," +
                            "    sr_return_tax DECIMAL(7,2)," +
                            "    sr_return_amt_inc_tax DECIMAL(7,2)," +
                            "    sr_fee DECIMAL(7,2)," +
                            "    sr_return_ship_cost DECIMAL(7,2)," +
                            "    sr_refunded_cash DECIMAL(7,2)," +
                            "    sr_reversed_charge DECIMAL(7,2)," +
                            "    sr_store_credit DECIMAL(7,2)," +
                            "    sr_net_loss DECIMAL(7,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.STORE_RETURNS, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition STORE_SALES =
            HiveTableDefinition.builder("store_sales")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    ss_sold_date_sk BIGINT," +
                            "    ss_sold_time_sk BIGINT," +
                            "    ss_item_sk BIGINT," +
                            "    ss_customer_sk BIGINT," +
                            "    ss_cdemo_sk BIGINT," +
                            "    ss_hdemo_sk BIGINT," +
                            "    ss_addr_sk BIGINT," +
                            "    ss_store_sk BIGINT," +
                            "    ss_promo_sk BIGINT," +
                            "    ss_ticket_number BIGINT," +
                            "    ss_quantity INT," +
                            "    ss_wholesale_cost DECIMAL(7,2)," +
                            "    ss_list_price DECIMAL(7,2)," +
                            "    ss_sales_price DECIMAL(7,2)," +
                            "    ss_ext_discount_amt DECIMAL(7,2)," +
                            "    ss_ext_sales_price DECIMAL(7,2)," +
                            "    ss_ext_wholesale_cost DECIMAL(7,2)," +
                            "    ss_ext_list_price DECIMAL(7,2)," +
                            "    ss_ext_tax DECIMAL(7,2)," +
                            "    ss_coupon_amt DECIMAL(7,2)," +
                            "    ss_net_paid DECIMAL(7,2)," +
                            "    ss_net_paid_inc_tax DECIMAL(7,2)," +
                            "    ss_net_profit DECIMAL(7,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.STORE_SALES, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition TIME_DIM =
            HiveTableDefinition.builder("time_dim")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    t_time_sk BIGINT," +
                            "    t_time_id CHAR(16)," +
                            "    t_time INT," +
                            "    t_hour INT," +
                            "    t_minute INT," +
                            "    t_second INT," +
                            "    t_am_pm CHAR(2)," +
                            "    t_shift CHAR(20)," +
                            "    t_sub_shift CHAR(20)," +
                            "    t_meal_time CHAR(20))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.TIME_DIM, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition WAREHOUSE =
            HiveTableDefinition.builder("warehouse")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    w_warehouse_sk BIGINT," +
                            "    w_warehouse_id CHAR(16)," +
                            "    w_warehouse_name VARCHAR(20)," +
                            "    w_warehouse_sq_ft INT," +
                            "    w_street_number CHAR(10)," +
                            "    w_street_name VARCHAR(60)," +
                            "    w_street_type CHAR(15)," +
                            "    w_suite_number CHAR(10)," +
                            "    w_city VARCHAR(60)," +
                            "    w_county VARCHAR(30)," +
                            "    w_state CHAR(2)," +
                            "    w_zip CHAR(10)," +
                            "    w_country VARCHAR(20)," +
                            "    w_gmt_offset DECIMAL(5,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.WAREHOUSE, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition WEB_PAGE =
            HiveTableDefinition.builder("web_page")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    wp_web_page_sk BIGINT," +
                            "    wp_web_page_id CHAR(16)," +
                            "    wp_rec_start_date DATE," +
                            "    wp_rec_end_date DATE," +
                            "    wp_creation_date_sk BIGINT," +
                            "    wp_access_date_sk BIGINT," +
                            "    wp_autogen_flag CHAR(1)," +
                            "    wp_customer_sk BIGINT," +
                            "    wp_url VARCHAR(1)," +
                            "    wp_type CHAR(50)," +
                            "    wp_char_count INT," +
                            "    wp_link_count INT," +
                            "    wp_image_count INT," +
                            "    wp_max_ad_count INT)" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.WEB_PAGE, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition WEB_RETURNS =
            HiveTableDefinition.builder("web_returns")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    wr_returned_date_sk BIGINT," +
                            "    wr_returned_time_sk BIGINT," +
                            "    wr_item_sk BIGINT," +
                            "    wr_refunded_customer_sk BIGINT," +
                            "    wr_refunded_cdemo_sk BIGINT," +
                            "    wr_refunded_hdemo_sk BIGINT," +
                            "    wr_refunded_addr_sk BIGINT," +
                            "    wr_returning_customer_sk BIGINT," +
                            "    wr_returning_cdemo_sk BIGINT," +
                            "    wr_returning_hdemo_sk BIGINT," +
                            "    wr_returning_addr_sk BIGINT," +
                            "    wr_web_page_sk BIGINT," +
                            "    wr_reason_sk BIGINT," +
                            "    wr_order_number BIGINT," +
                            "    wr_return_quantity INT," +
                            "    wr_return_amt DECIMAL(7,2)," +
                            "    wr_return_tax DECIMAL(7,2)," +
                            "    wr_return_amt_inc_tax DECIMAL(7,2)," +
                            "    wr_fee DECIMAL(7,2)," +
                            "    wr_return_ship_cost DECIMAL(7,2)," +
                            "    wr_refunded_cash DECIMAL(7,2)," +
                            "    wr_reversed_charge DECIMAL(7,2)," +
                            "    wr_account_credit DECIMAL(7,2)," +
                            "    wr_net_loss DECIMAL(7,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.WEB_RETURNS, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition WEB_SALES =
            HiveTableDefinition.builder("web_sales")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    ws_sold_date_sk BIGINT," +
                            "    ws_sold_time_sk BIGINT," +
                            "    ws_ship_date_sk BIGINT," +
                            "    ws_item_sk BIGINT," +
                            "    ws_bill_customer_sk BIGINT," +
                            "    ws_bill_cdemo_sk BIGINT," +
                            "    ws_bill_hdemo_sk BIGINT," +
                            "    ws_bill_addr_sk BIGINT," +
                            "    ws_ship_customer_sk BIGINT," +
                            "    ws_ship_cdemo_sk BIGINT," +
                            "    ws_ship_hdemo_sk BIGINT," +
                            "    ws_ship_addr_sk BIGINT," +
                            "    ws_web_page_sk BIGINT," +
                            "    ws_web_site_sk BIGINT," +
                            "    ws_ship_mode_sk BIGINT," +
                            "    ws_warehouse_sk BIGINT," +
                            "    ws_promo_sk BIGINT," +
                            "    ws_order_number BIGINT," +
                            "    ws_quantity INT," +
                            "    ws_wholesale_cost DECIMAL(7,2)," +
                            "    ws_list_price DECIMAL(7,2)," +
                            "    ws_sales_price DECIMAL(7,2)," +
                            "    ws_ext_discount_amt DECIMAL(7,2)," +
                            "    ws_ext_sales_price DECIMAL(7,2)," +
                            "    ws_ext_wholesale_cost DECIMAL(7,2)," +
                            "    ws_ext_list_price DECIMAL(7,2)," +
                            "    ws_ext_tax DECIMAL(7,2)," +
                            "    ws_coupon_amt DECIMAL(7,2)," +
                            "    ws_ext_ship_cost DECIMAL(7,2)," +
                            "    ws_net_paid DECIMAL(7,2)," +
                            "    ws_net_paid_inc_tax DECIMAL(7,2)," +
                            "    ws_net_paid_inc_ship DECIMAL(7,2)," +
                            "    ws_net_paid_inc_ship_tax DECIMAL(7,2)," +
                            "    ws_net_profit DECIMAL(7,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.WEB_SALES, 1))
                    .inSchema("tpcds")
                    .build();

    @RepositoryTableDefinition
    public static final HiveTableDefinition WEB_SITE =
            HiveTableDefinition.builder("web_site")
                    .setCreateTableDDLTemplate("" +
                            "CREATE %EXTERNAL% TABLE %NAME%(" +
                            "    web_site_sk BIGINT," +
                            "    web_site_id CHAR(16)," +
                            "    web_rec_start_date DATE," +
                            "    web_rec_end_date DATE," +
                            "    web_name VARCHAR(50)," +
                            "    web_open_date_sk BIGINT," +
                            "    web_close_date_sk BIGINT," +
                            "    web_class VARCHAR(50)," +
                            "    web_manager VARCHAR(40)," +
                            "    web_mkt_id INT," +
                            "    web_mkt_class VARCHAR(50)," +
                            "    web_mkt_desc VARCHAR(1)," +
                            "    web_market_manager VARCHAR(40)," +
                            "    web_company_id INT," +
                            "    web_company_name CHAR(50)," +
                            "    web_street_number CHAR(10)," +
                            "    web_street_name VARCHAR(60)," +
                            "    web_street_type CHAR(15)," +
                            "    web_suite_number CHAR(10)," +
                            "    web_city VARCHAR(60)," +
                            "    web_county VARCHAR(30)," +
                            "    web_state CHAR(2)," +
                            "    web_zip CHAR(10)," +
                            "    web_country VARCHAR(20)," +
                            "    web_gmt_offset DECIMAL(5,2)," +
                            "    web_tax_percentage DECIMAL(5,2))" +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'")
                    .setDataSource(new TpcdsDataSource(TpcdsTable.WEB_SITE, 1))
                    .inSchema("tpcds")
                    .build();

    private TpcdsTableDefinitions()
    {
    }
}
