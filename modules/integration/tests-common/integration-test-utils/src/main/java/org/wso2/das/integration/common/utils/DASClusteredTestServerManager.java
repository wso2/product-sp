/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.das.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.automation.test.utils.common.FileManager;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This class controls the DAS clustered integration tests
 */

public class DASClusteredTestServerManager extends TestServerManager {

    private static final Log log = LogFactory.getLog(DASClusteredTestServerManager.class);

    private AutomationContext context;
    private List<FileReplacementInformation> fileReplacementInformationList;

    public DASClusteredTestServerManager(AutomationContext context, int portOffset,
                                         List<FileReplacementInformation> fileReplacementInformationList) {
        super(context, portOffset);
        this.context = context;
        this.fileReplacementInformationList = fileReplacementInformationList;
    }

    @Override
    public void configureServer() throws AutomationFrameworkException {
        for (FileReplacementInformation fileReplacement : fileReplacementInformationList) {
            try {
                // if the carbonHome is empty, set the current carbon home
                if (fileReplacement.getCarbonHome().isEmpty()){
                    fileReplacement.setCarbonHome(this.getCarbonHome());
                }
                replaceFiles(fileReplacement.getSourceURL(),
                             fileReplacement.getPlaceHolderMap(fileReplacement.getCarbonHome(), this.getLocalhostIP()),
                             fileReplacement.getDestination());
            } catch (Exception e) {
                throw new AutomationFrameworkException("Unable to replace the configuration file : "
                                                       + fileReplacement.getSourceURL(), e);
            }
        }
    }

    public AutomationContext getContext() {
        return this.context;
    }

    private void replaceFiles(URL sourceURL, Map<String, String> placeHolder, String destination)
            throws URISyntaxException, IOException {
        String content = FileManager.readFile(new File(sourceURL.toURI()));
        for (String key : placeHolder.keySet()) {
            content = content.replace(key, placeHolder.get(key));
        }
        FileManager.deleteFile(this.getCarbonHome() + File.separator + destination);
        FileManager.writeToFile(this.getCarbonHome() + File.separator + destination, content);
    }


    private String getLocalhostIP() {
        try {
            return org.apache.axis2.util.Utils.getIpAddress();
        } catch (SocketException e) {
            log.error("Unable to get the localhost IP ", e);
        }
        return null;
    }


//    public static void main(String[] args) {
//
//        DASClusteredTestServerManager manager = new DASClusteredTestServerManager(null, 0, "");
//        Map<String, Map<String, String>> temp =  manager.createPlaceHolders(0);
//        System.out.println(temp.toString());
//    }
}


