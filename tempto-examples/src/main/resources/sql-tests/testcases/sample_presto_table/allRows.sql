-- database: presto; tables: sample_jdbc_table; groups: presto_convention
-- delimiter: |; ignoreOrder: false; types: BIGINT|LONGNVARCHAR
--!
SELECT * FROM sample_jdbc_table ORDER BY id;
--!
1|C|
2|null|
3|A|
