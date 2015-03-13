define table ANALYTICS_SCRIPTS_TEST (server_name string, ip STRING, tenant INTEGER, sequence LONG, summary STRING);
define table ANALYTICS_SCRIPTS_INSERT_TEST (server_name string, ip STRING, tenant INTEGER, sequence LONG, summary STRING);
INSERT INTO ANALYTICS_SCRIPTS_INSERT_TEST select * from ANALYTICS_SCRIPTS_TEST;