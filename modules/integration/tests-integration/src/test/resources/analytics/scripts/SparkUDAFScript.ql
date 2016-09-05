CREATE TEMPORARY TABLE udafTest USING CarbonAnalytics OPTIONS (tableName "udafTest", schema "member DOUBLE");

INSERT INTO TABLE udafTest SELECT 2;
INSERT INTO TABLE udafTest SELECT 4;
INSERT INTO TABLE udafTest SELECT 8;
INSERT INTO TABLE udafTest SELECT 16;
INSERT INTO TABLE udafTest SELECT 32;

SELECT geometricMean(member) as geomMean FROM udafTest;