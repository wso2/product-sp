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

package org.sp.integration.tests.core.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Universal yaml parser class - parses yaml files
 */
@SuppressWarnings("ALL")
class GenericYamlParser {

    private Log log = LogFactory.getLog(GenericYamlParser.class);

    Map<String, String> yamlInitializer(String fileName) {

        Yaml yaml = new Yaml();
        Map<String, String> map = null;
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(new File(fileName));
            // Parse the YAML file and return the output as a series of Maps and Lists
            map = (Map<String, String>) yaml.load(inputStream);

        } catch (Exception e) {
            log.error("Error " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return map;
    }
}
