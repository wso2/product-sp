/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.sp.integration.tests.core.beans;

import java.util.HashMap;

/**
 * Deployment bean for test-deployment.yaml
 */
public class Deployment {
    private String deployScripts;
    private String name;
    private String repository;
    private String suite;
    private String unDeployScripts;
    private boolean enable;
    private String filePath;
    private HashMap<String, String> instanceMap;

    public String getDeployScripts() {
        return deployScripts;
    }

    public void setDeployScripts(String deployScripts) {
        this.deployScripts = deployScripts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public String getUnDeployScripts() {
        return unDeployScripts;
    }

    public void setUnDeployScripts(String unDeployScripts) {
        this.unDeployScripts = unDeployScripts;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public HashMap getInstanceMap() {
        return instanceMap;
    }

    public void setInstanceMap(HashMap<String, String> instanceMap) {
        this.instanceMap = instanceMap;
    }
}
