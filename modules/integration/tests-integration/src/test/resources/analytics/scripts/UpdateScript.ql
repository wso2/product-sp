define table ANALYTICS_SCRIPTS_TEST (server_name string, ip STRING, tenant INTEGER, sequence LONG, summary STRING);

SELECT ip FROM ANALYTICS_SCRIPTS_TEST;

SELECT server_name, count(*) FROM ANALYTICS_SCRIPTS_TEST GROUP BY server_name;