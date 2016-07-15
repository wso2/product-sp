create temporary table test1 using CarbonJDBC options (dataSource "WSO2_ANALYTICS_EVENT_STORE_DB", tableName "test1",schema "us_state STRING, polarity INTEGER -i, usage_avg FLOAT", primaryKeys "us_state");

INSERT OVERWRITE TABLE test1 SELECT "Washington", 1, 428.66;
INSERT INTO TABLE test1 SELECT "North Dakota", 2, 173.54;
INSERT INTO TABLE test1 SELECT "New Hampshire", 3, 835.46;
INSERT INTO TABLE test1 SELECT "Ohio", 4, 224.97;
INSERT INTO TABLE test1 SELECT "Oklahoma", 5, 943.45;
INSERT INTO TABLE test1 SELECT "New Mexico", 6, 159.95;
INSERT INTO TABLE test1 SELECT "Colorado", 7, 732.54;
INSERT INTO TABLE test1 SELECT "Indiana", 8, 833.77;
INSERT INTO TABLE test1 SELECT "Florida", 9, 588.43;
INSERT INTO TABLE test1 SELECT "Maine", 0, 664.59;

SELECT * FROM test1;
