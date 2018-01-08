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


/**
 * This is the class which holds the configuration of the analytics tracer.
 */
class AnalyticsTracerConfig {
    private String type;
    private String analyticsUserName;
    private String analyticsPassword;
    private String analyticsURL;
    private String analyticsAuthURL;

    AnalyticsTracerConfig(String type, String analyticsURL, String analyticsAuthURL, String analyticsUserName,
                          String analyticsPassword) {
        this.type = type;
        this.analyticsUserName = analyticsUserName;
        this.analyticsPassword = analyticsPassword;
        this.analyticsURL = analyticsURL;
        this.analyticsAuthURL = analyticsAuthURL;
    }

    String getType() {
        return type;
    }

    String getAnalyticsUserName() {
        return analyticsUserName;
    }

    String getAnalyticsPassword() {
        return analyticsPassword;
    }

    String getAnalyticsURL() {
        return analyticsURL;
    }

    String getAnalyticsAuthURL() {
        return analyticsAuthURL;
    }
}
