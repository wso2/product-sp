CREATE TABLE RM_SERVER_INSTANCE (
  name VARCHAR(128) NOT NULL,
  server_url VARCHAR(1024) NOT NULL,
  dbms_type VARCHAR(128) NOT NULL,
  instance_type VARCHAR(128) NOT NULL,
  server_category VARCHAR(128) NOT NULL,
  admin_username VARCHAR(128),
  admin_password VARCHAR(128),
  tenant_id INTEGER NOT NULL,
  UNIQUE (name, tenant_id),
  PRIMARY KEY (name)
);

CREATE TABLE RM_DATABASE (
  name VARCHAR(128) NOT NULL,
  rss_instance_name VARCHAR(128),
  tenant_id INTEGER,
  UNIQUE (name, rss_instance_name),
  PRIMARY KEY (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RM_SERVER_INSTANCE (name)
);

CREATE TABLE RM_DATABASE_USER (
  username VARCHAR(16) NOT NULL,
  rss_instance_name VARCHAR(128),
  tenant_id INTEGER,
  UNIQUE (username, rss_instance_name, tenant_id),
  PRIMARY KEY (username),
  FOREIGN KEY (rss_instance_name) REFERENCES RM_SERVER_INSTANCE (name)
);

CREATE TABLE RM_DATABASE_PROPERTY (
  name VARCHAR(128) NOT NULL,
  value TEXT,
  database_name VARCHAR(128),
  rss_instance_name VARCHAR(128),
  tenant_id INTEGER,
  UNIQUE (name, database_name, rss_instance_name, tenant_id),
  PRIMARY KEY (name),
  FOREIGN KEY (database_name) REFERENCES RM_DATABASE (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RM_SERVER_INSTANCE (name)
);

CREATE TABLE RM_USER_DATABASE_ENTRY (
  username VARCHAR(16),
  database_name VARCHAR(128),
  rss_instance_name VARCHAR(128),
  tenant_id INTEGER,
  PRIMARY KEY (username, database_name, rss_instance_name, tenant_id),
  FOREIGN KEY (username) REFERENCES RM_DATABASE_USER (username),
  FOREIGN KEY (database_name) REFERENCES RM_DATABASE (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RM_SERVER_INSTANCE (name)
);

CREATE TABLE RM_USER_DATABASE_PRIVILEGE (
  username VARCHAR(16),
  database_name VARCHAR(128),
  rss_instance_name VARCHAR(128),
  tenant_id INTEGER,
  select_priv CHAR(1) NOT NULL,
  insert_priv CHAR(1) NOT NULL,
  update_priv CHAR(1) NOT NULL,
  delete_priv CHAR(1) NOT NULL,
  create_priv CHAR(1) NOT NULL,
  drop_priv CHAR(1) NOT NULL,
  grant_priv CHAR(1) NOT NULL,
  references_priv CHAR(1) NOT NULL,
  index_priv CHAR(1) NOT NULL,
  alter_priv CHAR(1) NOT NULL,
  create_tmp_table_priv CHAR(1) NOT NULL,
  lock_tables_priv CHAR(1) NOT NULL,
  create_view_priv CHAR(1) NOT NULL,
  show_view_priv CHAR(1) NOT NULL,
  create_routine_priv CHAR(1) NOT NULL,
  alter_routine_priv CHAR(1) NOT NULL,
  execute_priv CHAR(1) NOT NULL,
  event_priv CHAR(1) NOT NULL,
  trigger_priv CHAR(1) NOT NULL,
  PRIMARY KEY (username, database_name, rss_instance_name, tenant_id),
  FOREIGN KEY (username) REFERENCES RM_DATABASE_USER (username),
  FOREIGN KEY (database_name) REFERENCES RM_DATABASE (name),
  FOREIGN KEY (rss_instance_name) REFERENCES RM_SERVER_INSTANCE (name)
);

CREATE TABLE RM_SYSTEM_DATABASE_COUNT (
  count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE RM_DB_PRIVILEGE_TEMPLATE (
  name VARCHAR(128),
  tenant_id INTEGER,
  PRIMARY KEY (name, tenant_id)
);

CREATE TABLE RM_DB_PRIVILEGE_TEMPLATE_ENTRY (
  template_name VARCHAR(128),
  tenant_id INTEGER,
  select_priv CHAR(1) NOT NULL,
  insert_priv CHAR(1) NOT NULL,
  update_priv CHAR(1) NOT NULL,
  delete_priv CHAR(1) NOT NULL,
  create_priv CHAR(1) NOT NULL,
  drop_priv CHAR(1) NOT NULL,
  grant_priv CHAR(1) NOT NULL,
  references_priv CHAR(1) NOT NULL,
  index_priv CHAR(1) NOT NULL,
  alter_priv CHAR(1) NOT NULL,
  create_tmp_table_priv CHAR(1) NOT NULL,
  lock_tables_priv CHAR(1) NOT NULL,
  create_view_priv CHAR(1) NOT NULL,
  show_view_priv CHAR(1) NOT NULL,
  create_routine_priv CHAR(1) NOT NULL,
  alter_routine_priv CHAR(1) NOT NULL,
  execute_priv CHAR(1) NOT NULL,
  event_priv CHAR(1) NOT NULL,
  trigger_priv CHAR(1) NOT NULL,
  PRIMARY KEY (template_name, tenant_id),
  FOREIGN KEY (template_name) REFERENCES RM_DB_PRIVILEGE_TEMPLATE (name)
);
