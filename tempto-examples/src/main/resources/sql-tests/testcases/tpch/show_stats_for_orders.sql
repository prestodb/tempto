-- database: presto; groups: stats, tpcds; tables: orders 
-- delimiter: |; ignoreOrder: true; types: LONGNVARCHAR|JAVA_OBJECT|DOUBLE|DOUBLE|DOUBLE
--! name: show stats for orders 
SHOW STATS FOR orders
--!
o_custkey|null|99996.00000000000|0.00000000000|null|
o_clerk|null|1000.00000000000|0.00000000000|null|
o_orderdate|null|2406.00000000000|0.00000000000|null|
o_shippriority|null|1.00000000000|0.00000000000|null|
o_totalprice|null|1464556.00000000000|0.00000000000|null|
o_orderkey|null|1500000.00000000000|0.00000000000|null|
o_orderstatus|null|3.00000000000|0.00000000000|null|
o_orderpriority|null|5.00000000000|0.00000000000|null|
o_comment|null|1482071.00000000000|0.00000000000|null|
null|null|null|null|1500000.00000000000|
