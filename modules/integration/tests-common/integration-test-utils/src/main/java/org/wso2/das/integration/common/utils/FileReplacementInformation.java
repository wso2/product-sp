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

import java.net.URL;
import java.util.Map;

/**
 * Created by nira on 5/24/16.
 */
public abstract class FileReplacementInformation {

    private URL sourceURL;
    private String destination;

    public void setCarbonHome(String carbonHome) {
        this.carbonHome = carbonHome;
    }

    private String carbonHome;

    public FileReplacementInformation(URL sourceURL, String destination, String carbonHome) {
        this.sourceURL = sourceURL;
        this.destination = destination;
        this.carbonHome = carbonHome;
    }

    public URL getSourceURL() {
        return sourceURL;
    }

    public String getDestination() {
        return destination;
    }

    public String getCarbonHome() {
        return carbonHome;
    }

    public abstract Map<String, String> getPlaceHolderMap(String initialCarbonHome, String localhostIP);
}

