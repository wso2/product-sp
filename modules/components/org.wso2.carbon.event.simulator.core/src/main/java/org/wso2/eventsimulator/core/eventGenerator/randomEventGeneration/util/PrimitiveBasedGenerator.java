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


import fabricator.Alphanumeric;
import fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean.PrimitiveBasedAttributeDto;

import java.text.DecimalFormat;

public class PrimitiveBasedGenerator {

    private static final Logger log = LoggerFactory.getLogger(PrimitiveBasedGenerator.class);

    private static final Alphanumeric alpha = Fabricator.alphaNumeric();

    private PrimitiveBasedGenerator() {}

    public static Object generatePrimitiveBasedData(PrimitiveBasedAttributeDto primitiveBasedAttributeDto) {
        Object dataValue = null;
        DecimalFormat format = new DecimalFormat();

        try {
            switch (primitiveBasedAttributeDto.getAttrType()) {
                case INT:
                    dataValue = alpha.randomInt(Integer.parseInt(primitiveBasedAttributeDto.getMin()), Integer.parseInt(primitiveBasedAttributeDto.getMax()));
                    break;
                case LONG:
                    dataValue = alpha.randomLong(Long.parseLong(primitiveBasedAttributeDto.getMin()), Long.parseLong(primitiveBasedAttributeDto.getMax()));
                    break;
                case FLOAT:
                    format.setMaximumFractionDigits(primitiveBasedAttributeDto.getLength());
                    //Format value to given no of decimals
                    dataValue = Float.parseFloat(format.format(alpha.randomFloat(Float.parseFloat(primitiveBasedAttributeDto.getMin()), Float.parseFloat(primitiveBasedAttributeDto.getMax()))));
                    break;
                case DOUBLE:
                    format.setMaximumFractionDigits(primitiveBasedAttributeDto.getLength());
                    //Format value to given no of decimals
                    dataValue = Double.parseDouble(format.format(alpha.randomFloat(Float.parseFloat(primitiveBasedAttributeDto.getMin()), Float.parseFloat(primitiveBasedAttributeDto.getMax()))));
                    break;
                case STRING:
                    dataValue = alpha.randomString(primitiveBasedAttributeDto.getLength());
                    break;
                case BOOL:
                    dataValue = alpha.randomBoolean();
                    break;
            }
        } catch (Exception e) {
            log.error("Error occurred when generating primitive based data : " + e.getMessage());
        }
        return dataValue;
    }
}
