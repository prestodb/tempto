-- database: presto; groups: stats, tpch; tables: partsupp
-- delimiter: |; ignoreOrder: true; types: LONGNVARCHAR|JAVA_OBJECT|DOUBLE|DOUBLE|DOUBLE
--! name: show stats for partsupp
SHOW STATS FOR partsupp
--!
ps_availqty|null|9999.00000000000|0.00000000000|null|
ps_supplycost|null|99865.00000000000|0.00000000000|null|
ps_partkey|null|200000.00000000000|0.00000000000|null|
ps_suppkey|null|10000.00000000000|0.00000000000|null|
ps_comment|null|799124.00000000000|0.00000000000|null|
null|null|null|null|800000.00000000000|
