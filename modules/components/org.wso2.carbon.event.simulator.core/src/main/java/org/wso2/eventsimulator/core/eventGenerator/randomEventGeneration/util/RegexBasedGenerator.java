/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.RegexBasedAttributeDto;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mifmif.common.regex.Generex;


public class RegexBasedGenerator {

    private static final Logger log = LoggerFactory.getLogger(RegexBasedGenerator.class);

    private RegexBasedGenerator() {
    }

    /**
     * Generate data according to given regular expression.
     * It uses  A Java library called Generex for generating String that match
     * databaseFeedSimulation given regular expression
     *
     * @param regexBasedAttributeDto containing attribute configuration for regex based attribute generation
     * @return Generated value as object
     * @see "https://github.com/mifmif/Generex"
     */
    public static Object generateRegexBasedData(RegexBasedAttributeDto regexBasedAttributeDto) {
        Generex generex = new Generex(regexBasedAttributeDto.getPattern());
        Object result;
        result = generex.random();
        return result;
    }

    /**
     * Validate Regular Expression
     *
     * @param regularExpression regularExpression
     */
    public static void validateRegularExpression(String regularExpression) {
        try {
            Pattern.compile(regularExpression);
        } catch (PatternSyntaxException e) {
            log.error("Invalid regular expression : '" + regularExpression + "'. Error: " + e.getMessage());
        }

    }
}
