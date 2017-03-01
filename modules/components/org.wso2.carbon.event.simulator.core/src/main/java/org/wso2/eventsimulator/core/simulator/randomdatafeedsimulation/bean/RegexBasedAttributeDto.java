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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean;

/**
 * RegexBasedAttributeDto represents the Random data generator based on given regular expression
 * <p>
 * Constant value to represent to this type is "REGEXBASED"
 * <p>
 * Eg for json string for configuration
 * <p>
 * {
 * "type": "REGEXBASED",
 * "pattern": "[+]?[0-9]*\\.?[0-9]+"
 * }
 */

public class RegexBasedAttributeDto extends FeedSimulationStreamAttributeDto {
    /**
     * Regular Expression which is used to generate random data
     */
    private String pattern;

    public RegexBasedAttributeDto(String type, String pattern) {
        super();
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        String configuration = "Type : Regex based, pattern : " + pattern ;
        return configuration;
    }
}
