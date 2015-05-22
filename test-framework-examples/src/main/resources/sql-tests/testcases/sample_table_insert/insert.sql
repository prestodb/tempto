-- database: hive; mutable_tables: sample_table|created; groups: insert
--!
INSERT INTO TABLE ${mutableTables.sample_table} VALUES (1, 'A');
SELECT * from ${mutableTables.sample_table};

--!
-- delimiter: |; ignoreOrder: false; types: INTEGER|VARCHAR
1|A|