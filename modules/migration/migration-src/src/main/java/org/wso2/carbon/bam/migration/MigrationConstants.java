package org.wso2.carbon.bam.migration;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class MigrationConstants {
    public static String MIGRATION_CONF_DIR = "conf";
    public static String MIGRATION_CONF_FILE = "cassandra-config.xml";

    public static String CLUSTER_OMELEMENT = "Cassandra-Cluster";
    public static String CLUSTERNAME_OMELEMENT = "clusterName";
    public static String NODES_OMELEMENT = "nodes";
    public static String USERNAME_OMELEMENT = "username";
    public static String PASSWORD_OMELEMENT = "password";

    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    public static final String META_KS = "META_KS";
}
