package org.wso2.carbon.bam.migration;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
public class CassandraMigration {

    private static Log log = LogFactory.getLog(CassandraMigration.class);

    public static void main(String[] args) {
       CassandraConfiguration cassandraConfiguration = getCassandraConfiguration();

       CassandraHostConfigurator cassandraHostConfigurator = new CassandraHostConfigurator(cassandraConfiguration.getNodes());

            Map<String, String> creds = new HashMap<String, String>();
                            creds.put(MigrationConstants.USERNAME_KEY, cassandraConfiguration.getUserName() );
                            creds.put(MigrationConstants.PASSWORD_KEY, cassandraConfiguration.getPassword());

       Cluster cluster  = HFactory.getOrCreateCluster(cassandraConfiguration.getClusterName(), cassandraHostConfigurator, creds);

        try{
            cluster.dropKeyspace(MigrationConstants.META_KS);

            log.info("----------------------------------------------------");
            log.info("MIGRATION SUCCESSFULL");
            log.info("----------------------------------------------------");
        }catch (Exception exception){
            log.error(exception.getMessage());
            log.info("----------------------------------------------------");
            log.info("MIGRATION FAILED");
            log.info("----------------------------------------------------");
        }
    }


    private static CassandraConfiguration getCassandraConfiguration() {
        OMElement cassandraCluster = loadConfigXML();
        CassandraConfiguration configuration = new CassandraConfiguration();
        if (null != cassandraCluster && cassandraCluster.getQName().getLocalPart().
                equalsIgnoreCase(MigrationConstants.CLUSTER_OMELEMENT)) {

                configuration.setClusterName(cassandraCluster.getText().trim());
                log.info("Cassandra cluster name: "+configuration.getClusterName());

                OMElement clusterName = cassandraCluster.getFirstChildWithName(
                        new QName(MigrationConstants.CLUSTERNAME_OMELEMENT));
                if (null != clusterName) {
                    configuration.setClusterName(clusterName.getText());
                }

                OMElement nodes = cassandraCluster.getFirstChildWithName(
                        new QName(MigrationConstants.NODES_OMELEMENT));
                if (null != nodes) {
                    configuration.setNodes(nodes.getText());
                    log.info("Nodes : "+configuration.getNodes());
                }

                OMElement userName = cassandraCluster.getFirstChildWithName(
                        new QName(MigrationConstants.USERNAME_OMELEMENT));
                if (null != nodes) {
                    configuration.setUserName(userName.getText());
                     log.info("Username : "+configuration.getUserName());
                }

                OMElement password = cassandraCluster.getFirstChildWithName(
                        new QName(MigrationConstants.PASSWORD_OMELEMENT));
                if (null != nodes) {
                    configuration.setPassword(password.getText());
                    log.info("Password: "+ configuration.getPassword());
                }

        }
        return configuration;

    }

    private static OMElement loadConfigXML() {

        String path = ".."  + File.separator + MigrationConstants.MIGRATION_CONF_DIR +
                File.separator + MigrationConstants.MIGRATION_CONF_FILE;


        File cassandraConfigFile = new File(path);
        if (!cassandraConfigFile.exists()) {
            log.warn("No cassandra-config.xml found in "+cassandraConfigFile.getAbsolutePath());
            return null;
        }

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = MigrationConstants.MIGRATION_CONF_FILE
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + MigrationConstants.MIGRATION_CONF_FILE
                    + " located in the path : " + path;
            log.error(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
        return null;
    }


}
