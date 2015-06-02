-- database: hive; tables: sample_table; mutable_tables: sample_table|created|sample_table_created, sample_table|prepared|sample_table_prepared; groups: insert
-- delimiter: |; ignoreOrder: false; types: INTEGER|VARCHAR
--!
INSERT INTO TABLE ${mutableTables.sample_table_created} SELECT * from sample_table;
SELECT * from ${mutableTables.sample_table_created}
--!
1|A|
2|B|
3|C|
--!
CREATE TABLE ${mutableTables.sample_table_prepared} ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' AS SELECT * from sample_table;
SELECT * from ${mutableTables.sample_table_prepared}
--!
1|A|
2|B|
3|C|