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

package org.wso2.eventsimulator.core.generator.random.util;


import org.wso2.eventsimulator.core.generator.random.bean.PrimitiveBasedAttributeDto;
import org.wso2.eventsimulator.core.exception.EventGenerationException;

import java.text.DecimalFormat;

import fabricator.Alphanumeric;
import fabricator.Fabricator;

/**
 * PrimitiveBasedGenerator class is responsible for generating an attribute of primitive type
 */
public class PrimitiveBasedGenerator {

    private static final Alphanumeric alpha = Fabricator.alphaNumeric();

    private PrimitiveBasedGenerator() {
    }

    /**
     * generatePrimitiveBasedData() generates a primitive type attribute value based on the configuration provided by
     * primitiveBasedAttributeDto
     *
     * @param primitiveBasedAttributeDto configuration primitive based attribute generation
     * @return primitive attribute value
     */
    public static Object generatePrimitiveBasedData(PrimitiveBasedAttributeDto primitiveBasedAttributeDto) {
        Object dataValue = null;
        DecimalFormat format = new DecimalFormat();

        try {

            switch (primitiveBasedAttributeDto.getAttrType()) {
                case INT:
//                    generate a random integer between the minimum and maximum value specified
                    dataValue = alpha.randomInt(Integer.parseInt(primitiveBasedAttributeDto.getMin()),
                            Integer.parseInt(primitiveBasedAttributeDto.getMax()));
                    break;
                case LONG:
//                    generate a random long between the minimum and maximum value specified
                    dataValue = alpha.randomLong(Long.parseLong(primitiveBasedAttributeDto.getMin()),
                            Long.parseLong(primitiveBasedAttributeDto.getMax()));
                    break;
                case FLOAT:
                    /*
                    * generate a random float between the minimum and maximum value specified.
                    * the length defines the number of decimal places the float will have.
                    * */
                    format.setMaximumFractionDigits(primitiveBasedAttributeDto.getLength());
                    //Format value to given no of decimals
                    dataValue = (format.format(alpha.randomFloat(Float.parseFloat(primitiveBasedAttributeDto.getMin()),
                            Float.parseFloat(primitiveBasedAttributeDto.getMax())))).replace(",", "");
                    break;
                case DOUBLE:
                    /*
                    * generate a random float between the minimum and maximum value specified.
                    * the length defines the number of decimal places the float will have.
                    * the float value will then be parsed to a double
                    * */
                    format.setMaximumFractionDigits(primitiveBasedAttributeDto.getLength());
                    //Format value to given no of decimals
                    dataValue = (format.format(alpha.randomDouble(
                            Double.parseDouble(primitiveBasedAttributeDto.getMin()),
                            Double.parseDouble(primitiveBasedAttributeDto.getMax())))).replace(",", "");
                    break;
                case STRING:
//                    generate a random string of length specified
                    dataValue = alpha.randomString(primitiveBasedAttributeDto.getLength());
                    break;
                case BOOL:
//                    generate a random boolean
                    dataValue = alpha.randomBoolean();
                    break;
            }
        } catch (NumberFormatException e) {
            throw new EventGenerationException("Error occurred when creating a primitive based random event for " +
                    "primitive type '" + primitiveBasedAttributeDto.getAttrType() + "' : ", e);
        }

        return dataValue;
    }
}
