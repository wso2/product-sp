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

package org.wso2.carbon.event.simulator.core.internal.generator.random.util;

import com.mifmif.common.regex.Generex;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.RandomAttributeDTO;
import org.wso2.carbon.event.simulator.core.internal.bean.RegexBasedAttributeDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * RegexBasedGenerator is used to generate random data using regex provided
 */
public class RegexBasedGenerator implements RandomAttributeGenerator {

    private static final Logger log = LoggerFactory.getLogger(RegexBasedGenerator.class);

    private static RegexBasedAttributeDTO regexBasedAttrConfig =  new RegexBasedAttributeDTO();

    public RegexBasedGenerator() {
    }

    /**
     * Generate data according to given regular expression.
     * It uses  A Java library called Generex for generating String that match the given regular expression
     *
     * @return Generated value
     * @see "https://github.com/mifmif/Generex"
     */
    @Override
    public String generateAttribute() {
        Generex generex = new Generex(regexBasedAttrConfig.getPattern());
        return generex.random();
    }

    /**
     * validateAttributeConfig() validates the regex based attribute configuration provided
     * Validate Regular Expression for
     *
     * @param attributeConfig JSON object of the custom data attribute configuration
     * @throws InvalidConfigException if the regex has incorrect syntax
     */
    @Override
    public void validateAttributeConfig(JSONObject attributeConfig) throws InvalidConfigException {
        if (checkAvailability(attributeConfig, EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN)) {
          /*
          * validate regex pattern.
          * if pattern is valid, create a RegexBasedAttributeDTO object
          * else, throw an exception
          * */
            validateRegularExpression(attributeConfig.getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN));
            regexBasedAttrConfig.setPattern(attributeConfig
                    .getString(EventSimulatorConstants.REGEX_BASED_ATTRIBUTE_PATTERN));
        } else {
            throw new InvalidConfigException("Pattern is required for " +
                    RandomAttributeDTO.RandomDataGeneratorType.REGEX_BASED + " simulation. Invalid attribute " +
                    "configuration : " + attributeConfig.toString());
        }
    }

    /**
     * Validate Regular Expression
     *
     * @param regularExpression regularExpression
     * @throws InvalidConfigException if the regex has incorrect syntax
     */
    private void validateRegularExpression(String regularExpression) throws InvalidConfigException {
        try {
            Pattern.compile(regularExpression);
        } catch (PatternSyntaxException e) {
            log.error("Invalid regular expression : '" + regularExpression + "'.", e);
            throw new InvalidConfigException("Invalid regular expression : '" + regularExpression + "'.", e);
        }

    }
}
