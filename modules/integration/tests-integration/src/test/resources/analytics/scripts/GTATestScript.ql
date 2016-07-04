CREATE TEMPORARY TABLE Stats USING CarbonAnalytics OPTIONS (tableName "Stats", schema "name STRING, cnt INT, _tenantId INTEGER", globalTenantAccess "true");

INSERT INTO TABLE Stats SELECT "api1", 5, 1;
INSERT INTO TABLE Stats SELECT "api1", 7, 1;
INSERT INTO TABLE Stats SELECT "api2", 10, 1;
INSERT INTO TABLE Stats SELECT "api2", 14, 1;
INSERT INTO TABLE Stats SELECT "api1", 14, 2;
INSERT INTO TABLE Stats SELECT "api1", 2, 2;
INSERT INTO TABLE Stats SELECT "api3", 15, 2;
INSERT INTO TABLE Stats SELECT "api3", 5, 2;

CREATE TEMPORARY TABLE StatsSummary USING CarbonAnalytics OPTIONS (tableName "StatsSummary", schema "name STRING, cnt INT, _tenantId INTEGER", globalTenantAccess "true");

INSERT INTO TABLE StatsSummary SELECT name, SUM(cnt), _tenantId FROM Stats GROUP BY name, _tenantId;

SELECT * FROM StatsSummary;
