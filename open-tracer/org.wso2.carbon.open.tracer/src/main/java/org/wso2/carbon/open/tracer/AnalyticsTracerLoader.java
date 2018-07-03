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

import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

/**
 * This class is used to load the tracer from the Open tracing component extension.
 */
public class AnalyticsTracerLoader {
    private static AnalyticsTracerLoader instance = new AnalyticsTracerLoader();

    private AnalyticsTracerLoader() {

    }

    public static AnalyticsTracerLoader getInstance() {
        return instance;
    }

    public AnalyticsTracer getTracer(AnalyticsTracerConfig config)
            throws AnalyticsTracerInitializationException {
        try {
            DataPublisher dataPublisher = new DataPublisher(config.getType(), config.getAnalyticsURL(),
                    config.getAnalyticsAuthURL(), config.getAnalyticsUserName(), config.getAnalyticsPassword());
            return new AnalyticsTracer(dataPublisher, config.getComponentName());
        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException
                | DataEndpointAuthenticationException | TransportException e) {
            throw new AnalyticsTracerInitializationException("Error while initializing the data publisher" +
                    " for the analytics tracer.", e);
        }
    }
}
