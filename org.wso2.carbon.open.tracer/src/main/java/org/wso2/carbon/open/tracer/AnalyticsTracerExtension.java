/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.open.tracer;

import io.opentracing.Tracer;
import org.wso2.msf4j.opentracing.core.OpenTracer;

import org.wso2.msf4j.opentracing.core.config.InvalidConfigurationException;

import static org.wso2.carbon.open.tracer.Constants.AUTH_URL_CONFIG;
import static org.wso2.carbon.open.tracer.Constants.DEFAULT_AUTH_URL;
import static org.wso2.carbon.open.tracer.Constants.DEFAULT_PASSWORD;
import static org.wso2.carbon.open.tracer.Constants.DEFAULT_PUBLISHER_TYPE;
import static org.wso2.carbon.open.tracer.Constants.DEFAULT_URL;
import static org.wso2.carbon.open.tracer.Constants.DEFAULT_USERNAME;
import static org.wso2.carbon.open.tracer.Constants.PASSWORD_CONFIG;
import static org.wso2.carbon.open.tracer.Constants.PUBLISHER_TYPE_CONFIG;
import static org.wso2.carbon.open.tracer.Constants.TRACER_NAME;
import static org.wso2.carbon.open.tracer.Constants.URL_CONFIG;
import static org.wso2.carbon.open.tracer.Constants.USERNAME_CONFIG;

import java.util.Properties;

/**
 * This is the Open tracer extension for the {@link OpenTracer} which loads the tracer for the MSF4J.
 */
public class AnalyticsTracerExtension implements OpenTracer {
    @Override
    public Tracer getTracer(String tracerName, Properties properties) throws InvalidConfigurationException {
        if (!tracerName.equalsIgnoreCase(TRACER_NAME)) {
            throw new InvalidConfigurationException("Unexpected tracer name! " +
                    "The tracer name supported by this extension is : " + TRACER_NAME + " but found : "
                    + tracerName);
        }
        validateConfiguration(properties);
        AnalyticsTracerConfig tracerConfig = new AnalyticsTracerConfig(
                properties.getProperty(PUBLISHER_TYPE_CONFIG),
                properties.getProperty(URL_CONFIG),
                properties.getProperty(AUTH_URL_CONFIG),
                properties.getProperty(USERNAME_CONFIG),
                properties.getProperty(PASSWORD_CONFIG)
        );
        try {
            return AnalyticsTracerLoader.getInstance().getTracer(tracerConfig);
        } catch (AnalyticsTracerInitializationException e) {
            throw new InvalidConfigurationException("Unable to initialize the " + tracerName + " tracer.", e);
        }
    }

    private void validateConfiguration(Properties configuration) {
        setValidatedStringConfig(configuration, USERNAME_CONFIG, DEFAULT_USERNAME);
        setValidatedStringConfig(configuration, PASSWORD_CONFIG, DEFAULT_PASSWORD);
        setValidatedStringConfig(configuration, URL_CONFIG, DEFAULT_URL);
        setValidatedStringConfig(configuration, AUTH_URL_CONFIG, DEFAULT_AUTH_URL);
        setValidatedStringConfig(configuration, PUBLISHER_TYPE_CONFIG, DEFAULT_PUBLISHER_TYPE);
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
