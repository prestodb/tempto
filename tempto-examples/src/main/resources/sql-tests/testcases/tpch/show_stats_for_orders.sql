-- database: presto; groups: stats, tpcds; tables: orders 
-- delimiter: |; ignoreOrder: true; types: LONGNVARCHAR|JAVA_OBJECT|DOUBLE|DOUBLE|DOUBLE
--! name: show stats for orders 
SHOW STATS FOR orders
--!
o_orderpriority|null|5.0|0.0|null|
o_orderstatus|null|3.0|0.0|null|
o_orderkey|null|1500000.0|0.0|null|
o_custkey|null|99996.0|0.0|null|
o_totalprice|null|1464556.0|0.0|null|
o_shippriority|null|1.0|0.0|null|
o_comment|null|1482071.0|0.0|null|
o_clerk|null|1000.0|0.0|null|
o_orderdate|null|null|null|null|
null|null|null|null|1500000.0|
