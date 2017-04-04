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

import fabricator.Alphanumeric;
import fabricator.Fabricator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.PrimitiveBasedAttributeDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.siddhi.query.api.definition.Attribute;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import java.text.DecimalFormat;

/**
 * PrimitiveBasedAttrGenerator class is responsible for generating an attribute of primitive type
 */
public class PrimitiveBasedAttrGenerator implements RandomAttributeGenerator {
    private static final Logger log = LoggerFactory.getLogger(PrimitiveBasedAttrGenerator.class);
    private static final Alphanumeric alpha = Fabricator.alphaNumeric();
    private PrimitiveBasedAttributeDTO primitiveBasedAttrConfig = new PrimitiveBasedAttributeDTO();

    /**
     * PrimitiveBasedAttrGenerator() constructor validates the primitive based attribute configuration provided and
     * creates a PrimitiveBasedAttributeDTO object containing configuration required for primitive based attribute
     * generation
     *
     * @param attributeConfig JSON object of the primitive based attribute configuration
     * @throws InvalidConfigException if attribute configuration provided is invalid
     */
    public PrimitiveBasedAttrGenerator(JSONObject attributeConfig) throws InvalidConfigException {
        /**
         * retrieve the primitive type that need to be produced by primitive based random data
         * generator.
         * switch by the type so that only the required properties will be access and it will
         * also enable to provide more meaningful exceptions indicating which properties
         * are needed
         *
         * Given below are the properties needed for each primitive type generation
         * BOOL - none
         * STRING - length value
         * INT, LONG - min and max value
         * FLOAT, DOUBLE - min, max and length value.
         *
         * since Min and mx values are used by 4 primitive types, its saved as a string so that it could
         * later be parsed to the required primitive type
         **/
        Attribute.Type attrType;
        try {
            attrType = Attribute.Type.valueOf(
                    attributeConfig.getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE));
        } catch (IllegalArgumentException e) {
            throw new InvalidConfigException("Invalid attribute type '" +
                    attributeConfig.getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_TYPE)
                    + "' specified in attribute configuration for " +
                    RandomAttributeGenerator.RandomDataGeneratorType.PRIMITIVE_BASED + "' simulation. Invalid " +
                    "attribute configuration provided : " + attributeConfig.toString());
        }
        primitiveBasedAttrConfig.setAttrType(attrType);
        switch (attrType) {
            case BOOL:
                break;
            case STRING:
                if (checkAvailability(attributeConfig,
                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH)) {
                    primitiveBasedAttrConfig.setLength(attributeConfig
                            .getInt(EventSimulatorConstants.
                                    PRIMITIVE_BASED_ATTRIBUTE_LENGTH));
                } else {
                    throw new InvalidConfigException("Property 'Length' is required for generation of attributes of " +
                            "type '" + attrType + "' in " + RandomAttributeGenerator.RandomDataGeneratorType
                            .PRIMITIVE_BASED + " attribute generation. Invalid attribute configuration provided : " +
                            attributeConfig.toString());
                }
                break;
            case INT:
            case LONG:
                if (checkAvailability(attributeConfig, EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                        && checkAvailability(attributeConfig, EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)) {
                    primitiveBasedAttrConfig.setMin(attributeConfig
                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                    primitiveBasedAttrConfig.setMax(attributeConfig
                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));
                } else {
                    throw new InvalidConfigException("Properties 'Min' and 'Max' are required " +
                            "for generation of attributes of  type '" + attrType + "' in" +
                            RandomAttributeGenerator.RandomDataGeneratorType.PRIMITIVE_BASED +
                            " attribute generation. Invalid attribute configuration provided" +
                            ": " + attributeConfig.toString());
                }
                break;
            case FLOAT:
            case DOUBLE:
                if (checkAvailability(attributeConfig, EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN)
                        && checkAvailability(attributeConfig, EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX)
                        && checkAvailability(attributeConfig,
                        EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_LENGTH)) {
                    primitiveBasedAttrConfig.setMin(attributeConfig
                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MIN));
                    primitiveBasedAttrConfig.setMax(attributeConfig
                            .getString(EventSimulatorConstants.PRIMITIVE_BASED_ATTRIBUTE_MAX));
                    primitiveBasedAttrConfig.setLength(attributeConfig
                            .getInt(EventSimulatorConstants
                                    .PRIMITIVE_BASED_ATTRIBUTE_LENGTH));
                } else {
                    throw new InvalidConfigException("Properties 'Min','Max' and 'Length' are " +
                            "required for generation of attributes of type '" + attrType + "' in " +
                            RandomAttributeGenerator.RandomDataGeneratorType.PRIMITIVE_BASED +
                            " attribute generation. Invalid attribute configuration provided : " +
                            attributeConfig.toString());
                }
                break;
            default:
//                this statement is never reached since attribute type is an enum
        }
    }

    /**
     * generatePrimitiveBasedData() generates a primitive type attribute value based on the configuration provided by
     * primitiveBasedAttrConfig
     *
     * @return primitive attribute value
     */
    @Override
    public Object generateAttribute() {
        Object dataValue = null;
        try {
            switch (primitiveBasedAttrConfig.getAttrType()) {
                case INT:
//                    generate a random integer between the minimum and maximum value specified
                    dataValue = alpha.randomInt(Integer.parseInt(primitiveBasedAttrConfig.getMin()),
                            Integer.parseInt(primitiveBasedAttrConfig.getMax()));
                    break;
                case LONG:
//                    generate a random long between the minimum and maximum value specified
                    dataValue = alpha.randomLong(Long.parseLong(primitiveBasedAttrConfig.getMin()),
                            Long.parseLong(primitiveBasedAttrConfig.getMax()));
                    break;
                case FLOAT:
                    /**
                     * generate a random float between the minimum and maximum value specified.
                     * the length defines the number of decimal places the float will have.
                     * */
                    DecimalFormat format = new DecimalFormat();
                    format.setMaximumFractionDigits(primitiveBasedAttrConfig.getLength());
//                    Format value to given no of decimals
                    dataValue = (format.format(alpha.randomFloat(Float.parseFloat(primitiveBasedAttrConfig.getMin()),
                            Float.parseFloat(primitiveBasedAttrConfig.getMax())))).replace(",", "");
                    break;
                case DOUBLE:
                    /**
                     * generate a random float between the minimum and maximum value specified.
                     * the length defines the number of decimal places the float will have.
                     * the float value will then be parsed to a double
                     * */
                    DecimalFormat formatDouble = new DecimalFormat();
                    formatDouble.setMaximumFractionDigits(primitiveBasedAttrConfig.getLength());
//                    Format value to given no of decimals
                    dataValue = (formatDouble.format(alpha.randomDouble(
                            Double.parseDouble(primitiveBasedAttrConfig.getMin()),
                            Double.parseDouble(primitiveBasedAttrConfig.getMax())))).replace(",", "");
                    break;
                case STRING:
//                    generate a random string of length specified
                    dataValue = alpha.randomString(primitiveBasedAttrConfig.getLength());
                    break;
                case BOOL:
//                    generate a random boolean
                    dataValue = alpha.randomBoolean();
                    break;
                default:
//                    this statement is never reached since attribute type is an enum
            }
        } catch (NumberFormatException e) {
            log.error("Error occurred when creating a primitive based random data attribute " +
                    "of primitive type '" + primitiveBasedAttrConfig.getAttrType() + "' for attribute configuration :" +
                    primitiveBasedAttrConfig.toString() + "'. ", e);
            throw new EventGenerationException("Error occurred when creating a primitive based random data attribute " +
                    "of primitive type '" + primitiveBasedAttrConfig.getAttrType() + "' for attribute configuration :" +
                    primitiveBasedAttrConfig.toString() + "'. ", e);
        }
        return dataValue;
    }
}
