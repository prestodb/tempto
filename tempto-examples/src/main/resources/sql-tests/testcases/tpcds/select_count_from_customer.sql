-- database: presto_tpcds; groups: tpcds; tables: tpcds.customer
-- delimiter: |; ignoreOrder: false; types: BIGINT
--! name: query_1
SELECT count(*) FROM customer
--!
100000|
