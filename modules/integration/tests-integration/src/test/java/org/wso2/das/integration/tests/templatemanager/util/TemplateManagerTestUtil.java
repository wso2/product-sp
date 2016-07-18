/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.das.integration.tests.templatemanager.util;

import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class TemplateManagerTestUtil {

    public static Boolean isEventSinkExists(String streamName) {
        File eventSinkFile = new File(ServerConfigurationManager.getCarbonHome() + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator
                + "eventsink" + File.separator + streamName + ".xml");
        if (eventSinkFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static AnalyticsEventStore getExistingEventStore(String streamName) throws JAXBException {
        File eventSinkFile = new File(ServerConfigurationManager.getCarbonHome() + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator
                + "eventsink" + File.separator + streamName + ".xml");
        if (eventSinkFile.exists()) {
            JAXBContext context = JAXBContext.newInstance(AnalyticsEventStore.class);
            Unmarshaller un = context.createUnmarshaller();
            return (AnalyticsEventStore) un.unmarshal(eventSinkFile);
        }
        return null;
    }
}
