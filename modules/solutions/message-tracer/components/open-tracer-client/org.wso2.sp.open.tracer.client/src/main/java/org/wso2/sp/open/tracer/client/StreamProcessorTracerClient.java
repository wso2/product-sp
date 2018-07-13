/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.sp.open.tracer.client;

import io.opentracing.ScopeManager;
import io.opentracing.Tracer;
import org.wso2.carbon.databridge.agent.AgentHolder;

import java.util.Properties;

import static org.wso2.sp.open.tracer.client.Constants.AUTH_URL_CONFIG;
import static org.wso2.sp.open.tracer.client.Constants.DEFAULT_AUTH_URL;
import static org.wso2.sp.open.tracer.client.Constants.DEFAULT_PASSWORD;
import static org.wso2.sp.open.tracer.client.Constants.DEFAULT_PUBLISHER_TYPE;
import static org.wso2.sp.open.tracer.client.Constants.DEFAULT_URL;
import static org.wso2.sp.open.tracer.client.Constants.DEFAULT_USERNAME;
import static org.wso2.sp.open.tracer.client.Constants.PASSWORD_CONFIG;
import static org.wso2.sp.open.tracer.client.Constants.PUBLISHER_TYPE_CONFIG;
import static org.wso2.sp.open.tracer.client.Constants.TRACER_NAME;
import static org.wso2.sp.open.tracer.client.Constants.TRACER_VALUE;
import static org.wso2.sp.open.tracer.client.Constants.URL_CONFIG;
import static org.wso2.sp.open.tracer.client.Constants.USERNAME_CONFIG;
import static org.wso2.sp.open.tracer.client.Constants.WSO2SP_REPORTER_DATABRIDGE_AGENT_CONFIG;
import static org.wso2.sp.open.tracer.client.Constants.WSO2SP_REPORTER_TRUSTSTORE;
import static org.wso2.sp.open.tracer.client.Constants.WSO2SP_REPORTER_TRUSTSTORE_PASSWORD;

/**
 * This is the Stream Processor Open tracer client which can be used to publish tracer events from services.
 */
public class StreamProcessorTracerClient {
    private Properties properties;

    public void init(Properties properties) throws InvalidTracerConfigurationException {
        this.properties = properties;
        if (!properties.getProperty(TRACER_NAME).equalsIgnoreCase(TRACER_VALUE)) {
            throw new InvalidTracerConfigurationException("Unexpected tracer name! " +
                    "The tracer name supported by this extension is : " + TRACER_NAME + " but found : "
                    + properties.getProperty(TRACER_NAME));
        }
        validateConfiguration(properties);
        System.setProperty(WSO2SP_REPORTER_TRUSTSTORE, properties.getProperty(WSO2SP_REPORTER_TRUSTSTORE));
        System.setProperty(WSO2SP_REPORTER_TRUSTSTORE_PASSWORD,
                properties.getProperty(WSO2SP_REPORTER_TRUSTSTORE_PASSWORD));
        AgentHolder.setConfigPath(properties.getProperty(WSO2SP_REPORTER_DATABRIDGE_AGENT_CONFIG));
    }
    public Tracer getTracer(String serviceName, ScopeManager scopeManager)
            throws AnalyticsTracerInitializationException {
        AnalyticsTracerConfig tracerConfig = new AnalyticsTracerConfig(
                properties.getProperty(PUBLISHER_TYPE_CONFIG),
                properties.getProperty(URL_CONFIG),
                properties.getProperty(AUTH_URL_CONFIG),
                properties.getProperty(USERNAME_CONFIG),
                properties.getProperty(PASSWORD_CONFIG),
                serviceName
        );
        Tracer tracer = AnalyticsTracerLoader.getInstance().getTracer(tracerConfig, scopeManager);
        return tracer;
    }

    private void validateConfiguration(Properties configuration) throws InvalidTracerConfigurationException {
        setValidatedStringConfig(configuration, USERNAME_CONFIG, DEFAULT_USERNAME);
        setValidatedStringConfig(configuration, PASSWORD_CONFIG, DEFAULT_PASSWORD);
        setValidatedStringConfig(configuration, URL_CONFIG, DEFAULT_URL);
        setValidatedStringConfig(configuration, AUTH_URL_CONFIG, DEFAULT_AUTH_URL);
        setValidatedStringConfig(configuration, PUBLISHER_TYPE_CONFIG, DEFAULT_PUBLISHER_TYPE);
        if (null == configuration.getProperty(WSO2SP_REPORTER_DATABRIDGE_AGENT_CONFIG)) {
            throw new InvalidTracerConfigurationException("Databridge agent config location is needed for tracer: " +
                    TRACER_NAME + ".");
        } else if (null == configuration.getProperty(WSO2SP_REPORTER_TRUSTSTORE)) {
            throw new InvalidTracerConfigurationException("Truststore location is needed for tracer: " +
                    TRACER_NAME + ".");
        }
    }

    private void setValidatedStringConfig(Properties configuration, String configName, String defaultValue) {
        Object configValue = configuration.get(configName);
        if (configValue == null || configValue.toString().trim().isEmpty()) {
            configuration.put(configName, defaultValue);
        } else {
            configuration.put(configName, configValue.toString().trim());
        }
    }
}
